package com.bc.gordiansigner.service

import com.bc.gordiansigner.helper.Network
import com.bc.gordiansigner.helper.ext.fromJson
import com.bc.gordiansigner.helper.ext.newGsonInstance
import com.bc.gordiansigner.model.Bip39Mnemonic
import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.service.storage.file.FileStorageApi
import com.bc.gordiansigner.service.storage.file.rxCompletable
import com.bc.gordiansigner.service.storage.file.rxSingle
import com.bc.gordiansigner.service.storage.sharedpref.SharedPrefApi
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.security.SecureRandom
import javax.inject.Inject

class WalletService @Inject constructor(
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : BaseService(sharedPrefApi, fileStorageApi) {

    companion object {
        private const val XPRIV_KEY_FILE = "xpriv.secret"
        private const val FINGERPRINT_FILE = "fingerprint.secret"
    }

    fun importHDKeyWallet(mnemonic: String, network: Network): Single<HDKey> {
        return Single.fromCallable {
            val seed = Bip39Mnemonic(mnemonic).seed
            HDKey(seed, network)
        }.subscribeOn(Schedulers.computation())
    }

    fun generateHDKeyWallet(network: Network): Single<HDKey> {
        return Single.fromCallable {
            val entropy = ByteArray(16)
            SecureRandom().nextBytes(entropy)
            val seed = Bip39Mnemonic(entropy).seed
            HDKey(seed, network)
        }.subscribeOn(Schedulers.computation())
    }

    fun getHDKey(fingerprint: String) =
        getHDKeyXprvs().map { keys -> keys.first { it.fingerprintHex == fingerprint } }

    fun getHDKeyXprvs() =
        fileStorageApi.SUPER_SECURE.rxSingle { gateway ->
            val privs = gateway.readOnFilesDir(XPRIV_KEY_FILE)
            if (privs.isEmpty()) {
                emptyList()
            } else {
                newGsonInstance().fromJson<List<String>>(String(privs))
            }
        }.map { set -> set.map { HDKey(it) } }

    fun getHDKeyFingerprints() = fileStorageApi.SECURE.rxSingle { gateway ->
        val fingerprints = gateway.readOnFilesDir(FINGERPRINT_FILE)
        if (fingerprints.isEmpty()) {
            emptyList()
        } else {
            newGsonInstance().fromJson<List<String>>(String(fingerprints))
        }
    }

    fun saveHDKeyXprv(hdKey: HDKey) =
        getHDKeyXprvs().flatMap { keys ->
            val keySet = keys.toMutableSet()
            if (keySet.add(hdKey)) {
                saveHDKeys(keySet).andThen(Single.just(hdKey))
            } else {
                Single.just(hdKey)
            }
        }

    fun deleteHDKey(fingerprintHex: String) = getHDKeyXprvs().flatMapCompletable { keys ->
        val fingerprints = keys.map { it.fingerprintHex }
        if (fingerprints.contains(fingerprintHex)) {
            val newKeys = keys.filterNot { it.fingerprintHex == fingerprintHex }.toSet()
            saveHDKeys(newKeys)
        } else {
            Completable.complete()
        }
    }

    private fun saveHDKeys(keys: Set<HDKey>) =
        Completable.mergeArray(fileStorageApi.SUPER_SECURE.rxCompletable { gateway ->
            gateway.writeOnFilesDir(
                XPRIV_KEY_FILE,
                newGsonInstance().toJson(keys.map { it.xprv }).toByteArray()
            )
        }, fileStorageApi.SECURE.rxCompletable { gateway ->
            gateway.writeOnFilesDir(
                FINGERPRINT_FILE,
                newGsonInstance().toJson(keys.map { it.fingerprintHex }).toByteArray()
            )
        })

}