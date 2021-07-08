package com.bc.gordiansigner.service

import com.bc.gordiansigner.helper.Hex
import com.bc.gordiansigner.helper.Network
import com.bc.gordiansigner.helper.ext.fromJson
import com.bc.gordiansigner.helper.ext.newGsonInstance
import com.bc.gordiansigner.model.Bip39Mnemonic
import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.service.storage.file.FileStorageApi
import com.bc.gordiansigner.service.storage.file.rxCompletable
import com.bc.gordiansigner.service.storage.file.rxSingle
import com.bc.gordiansigner.service.storage.sharedpref.SharedPrefApi
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.security.SecureRandom
import java.util.*
import javax.inject.Inject

class AccountService @Inject constructor(
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : BaseService(sharedPrefApi, fileStorageApi) {

    companion object {
        private const val SEED_KEY_FILE = "seed.secret"
        private const val KEY_INFO_FILE = "keyinfo.secret"
    }

    fun importMnemonic(
        mnemonic: String,
        network: Network = Network.TEST
    ): Single<Pair<String, HDKey>> {
        return Single.fromCallable {
            val seed = Bip39Mnemonic(mnemonic).seed
            Pair(Hex.hexFromBytes(seed), HDKey(seed, network))
        }.subscribeOn(Schedulers.computation())
    }

    fun generateMnemonic(): Single<String> {
        return Single.fromCallable {
            val entropy = ByteArray(16)
            SecureRandom().nextBytes(entropy)
            Bip39Mnemonic(entropy).mnemonic
        }.subscribeOn(Schedulers.computation())
    }

    fun getSeed(fingerprint: String, network: Network = Network.TEST) =
        getSeeds()
            .map { set -> set.map { Pair(it, HDKey(Hex.hexToBytes(it), network)) } }
            .map { set -> set.first { it.second.fingerprintHex == fingerprint } }
            .flatMap { updateKeyInfoLastUsed(it.second.fingerprintHex).andThen(Single.just(it.first)) }

    private fun getSeeds() = fileStorageApi.SUPER_SECURE.rxSingle { gateway ->
        val seeds = gateway.readOnFilesDir(SEED_KEY_FILE)
        if (seeds.isEmpty()) {
            emptyList()
        } else {
            newGsonInstance().fromJson<List<String>>(String(seeds))
        }
    }

    fun getKeysInfo() = fileStorageApi.SECURE.rxSingle { gateway ->
        val json = gateway.readOnFilesDir(KEY_INFO_FILE)
        if (json.isEmpty()) {
            emptyList()
        } else {
            newGsonInstance().fromJson<List<KeyInfo>>(String(json))
        }
    }

    fun saveSeedAndKeyInfo(keyInfo: KeyInfo, seed: String) = if (keyInfo.isSaved) {
        saveSeed(seed)
    } else {
        Single.just(seed)
    }.flatMap { saveKeyInfo(keyInfo).map { seed } }

    fun deleteSeed(fingerprintHex: String): Completable = getSeeds().flatMapCompletable { seeds ->
        seeds.firstOrNull {
            HDKey.fingerprintFromSeed(Hex.hexToBytes(it)) == fingerprintHex
        }?.let { seed ->
            val newSeeds = seeds.toMutableSet().apply { remove(seed) }
            saveSeeds(newSeeds).andThen(updateKeyInfoSavingState(fingerprintHex))
        } ?: Completable.complete()
    }

    fun deleteKeyInfo(fingerprintHex: String) = getKeysInfo().flatMapCompletable { keysInfo ->
        val currentKey = keysInfo.firstOrNull { it.fingerprint == fingerprintHex }
        currentKey?.let { keyInfo ->
            val newKeys = keysInfo.filterNot { it == keyInfo }.toSet()
            saveKeysInfo(newKeys).andThen(if (keyInfo.isSaved) deleteSeed(fingerprintHex) else Completable.complete())
        } ?: Completable.complete()
    }

    private fun updateKeyInfoSavingState(fingerprint: String): Completable =
        getKeysInfo().flatMapCompletable { keysInfo ->
            val keyInfoSet = keysInfo.toMutableSet()
            keyInfoSet.firstOrNull { it.fingerprint == fingerprint }?.let {
                it.isSaved = false
                saveKeysInfo(keyInfoSet)
            } ?: Completable.complete()
        }

    private fun updateKeyInfoLastUsed(fingerprint: String): Completable =
        getKeysInfo().flatMapCompletable { keysInfo ->
            val keyInfoSet = keysInfo.toMutableSet()
            keyInfoSet.firstOrNull { it.fingerprint == fingerprint }?.let {
                it.lastUsed = Date()
                saveKeysInfo(keyInfoSet)
            } ?: Completable.complete()
        }

    private fun saveSeed(seed: String) =
        getSeeds().flatMap { seeds ->
            val mutableSeeds = seeds.toMutableSet()
            if (mutableSeeds.add(seed)) {
                saveSeeds(mutableSeeds).andThen(Single.just(seed))
            } else {
                Single.just(seed)
            }
        }

    fun saveKeyInfo(keyInfo: KeyInfo) = getKeysInfo().flatMap { keysInfo ->
        val keyInfoSet = keysInfo.toMutableSet()
        keyInfoSet.firstOrNull { it == keyInfo }?.let {
            if (keyInfo.alias.isNotEmpty()) {
                it.alias = keyInfo.alias
            }
            it.isSaved = keyInfo.isSaved
            it.lastUsed = keyInfo.lastUsed
        } ?: let {
            keyInfoSet.add(keyInfo)
        }

        saveKeysInfo(keyInfoSet).andThen(Single.just(keyInfo))
    }

    private fun saveSeeds(seeds: Set<String>) =
        fileStorageApi.SUPER_SECURE.rxCompletable { gateway ->
            gateway.writeOnFilesDir(
                SEED_KEY_FILE,
                newGsonInstance().toJson(seeds.map { it }).toByteArray()
            )
        }

    private fun saveKeysInfo(keysInfo: Set<KeyInfo>) =
        fileStorageApi.SECURE.rxCompletable { gateway ->
            gateway.writeOnFilesDir(
                KEY_INFO_FILE,
                newGsonInstance().toJson(keysInfo).toByteArray()
            )
        }

}