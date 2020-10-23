package com.bc.gordiansigner.service

import com.bc.gordiansigner.model.HDKey
import com.bc.gordiansigner.model.Psbt
import com.bc.gordiansigner.service.storage.file.FileStorageApi
import com.bc.gordiansigner.service.storage.sharedpref.SharedPrefApi
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class TransactionService @Inject constructor(
    sharedPrefApi: SharedPrefApi,
    fileStorageApi: FileStorageApi
) : BaseService(sharedPrefApi, fileStorageApi) {

    fun signPsbt(psbt: Psbt, hdKey: HDKey): Single<String> = Single.fromCallable {
        val path = psbt.inputBip32Derivs.first { it.fingerprintHex == hdKey.fingerprintHex }.path
        psbt.sign(hdKey.derive(path))
        psbt.toBase64()
    }.subscribeOn(Schedulers.computation())

}