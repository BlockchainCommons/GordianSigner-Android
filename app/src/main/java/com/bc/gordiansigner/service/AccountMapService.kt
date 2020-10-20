package com.bc.gordiansigner.service

import com.bc.gordiansigner.helper.ext.newGsonInstance
import com.bc.gordiansigner.model.AccountMap
import com.bc.gordiansigner.model.Descriptor
import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.service.storage.file.FileStorageApi
import com.bc.gordiansigner.service.storage.sharedpref.SharedPrefApi
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AccountMapService @Inject constructor(
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : BaseService(sharedPrefApi, fileStorageApi) {

    fun saveAccountMap(accountMap: AccountMap): Single<AccountMap> {
        //TODO: save account map, implement later

        return Single.just(accountMap)
    }

    fun getAccountMapInfo(accountMapString: String): Single<Pair<AccountMap, Descriptor>> =
        Single.fromCallable {
            val accountMap = newGsonInstance().fromJson(accountMapString, AccountMap::class.java)
            val descriptor = Descriptor.fromString(accountMap.descriptor)

            Pair(accountMap, descriptor)
        }.subscribeOn(Schedulers.computation())

    fun fillPartialAccountMap(
        accountMap: AccountMap,
        descriptor: Descriptor,
        hdKey: HDKey
    ): Single<String> {
        return Single.fromCallable {
            if (!descriptor.isCompleted()) {
                descriptor.updatePartialAccountMapFromKey(hdKey)
            }
            AccountMap(descriptor.toString(), accountMap.blockheight, accountMap.label)
        }
            .subscribeOn(Schedulers.computation())
            .flatMap { saveAccountMap(it) }
            .map {
                newGsonInstance().toJson(it)
            }
            .subscribeOn(Schedulers.computation())
    }
}