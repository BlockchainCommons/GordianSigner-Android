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
            descriptor.updatePartialAccountMapFromKey(hdKey)
            AccountMap(descriptor.toString(), accountMap.blockheight, accountMap.label)
        }
            .map {
                newGsonInstance().toJson(it)
            }
            .subscribeOn(Schedulers.computation())
    }
}