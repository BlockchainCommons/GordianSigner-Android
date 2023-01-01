package com.bc.gordiansigner.service

import com.bc.gordiansigner.helper.ext.fromJson
import com.bc.gordiansigner.helper.ext.newGsonInstance
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.service.storage.file.FileStorageApi
import com.bc.gordiansigner.service.storage.file.rxCompletable
import com.bc.gordiansigner.service.storage.file.rxSingle
import com.bc.gordiansigner.service.storage.sharedpref.SharedPrefApi
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class ContactService @Inject constructor(
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : BaseService(sharedPrefApi, fileStorageApi) {

    companion object {
        private const val CONTACT_KEY_INFO_FILE = "contact_keyinfo.secret"
    }

    fun getContactKeysInfo() = fileStorageApi.SECURE.rxSingle { gateway ->
        val json = gateway.readOnFilesDir(CONTACT_KEY_INFO_FILE)
        if (json.isEmpty()) {
            emptyList()
        } else {
            newGsonInstance().fromJson<List<KeyInfo>>(String(json))
        }
    }

    fun deleteContactKeyInfo(fingerprintHex: String) =
        getContactKeysInfo().flatMapCompletable { keysInfo ->
            if (keysInfo.any { it.fingerprint == fingerprintHex }) {
                val newKeys = keysInfo.filterNot { it.fingerprint == fingerprintHex }.toSet()
                saveContactKeysInfo(newKeys)
            } else {
                Completable.complete()
            }
        }

    fun saveContactKeyInfo(keyInfo: KeyInfo) = getContactKeysInfo().flatMap { keysInfo ->
        val keyInfoSet = keysInfo.toMutableSet()
        var isCreating = true
        keyInfoSet.firstOrNull { it == keyInfo }?.let {
            it.alias = keyInfo.alias
            it.isSaved = keyInfo.isSaved
            isCreating = false
        } ?: let {
            keyInfoSet.add(keyInfo)
        }
        saveContactKeysInfo(keyInfoSet).andThen(Single.just(Pair(keyInfo, isCreating)))
    }

    fun appendContactKeysInfo(keysInfo: Set<KeyInfo>) =
        getContactKeysInfo().flatMapCompletable { existingKeysInfo ->
            val existingKeyInfoSet = existingKeysInfo.toMutableSet()
            existingKeyInfoSet.addAll(keysInfo)
            saveContactKeysInfo(existingKeyInfoSet)
        }

    private fun saveContactKeysInfo(keysInfo: Set<KeyInfo>) =
        fileStorageApi.SECURE.rxCompletable { gateway ->
            gateway.writeOnFilesDir(
                CONTACT_KEY_INFO_FILE,
                newGsonInstance().toJson(keysInfo).toByteArray()
            )
        }
}