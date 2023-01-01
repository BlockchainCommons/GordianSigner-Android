package com.bc.gordiansigner.ui.account

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.service.AccountService
import com.bc.gordiansigner.ui.BaseViewModel
import io.reactivex.Single

class AccountsViewModel(
    lifecycle: Lifecycle,
    private val accountService: AccountService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val keyInfoLiveData = CompositeLiveData<List<KeyInfo>>()
    internal val deleteKeysLiveData = CompositeLiveData<String>()
    internal val updateKeysLiveData = CompositeLiveData<KeyInfo>()
    internal val getSeedLiveData = CompositeLiveData<String>()

    fun fetchKeysInfo() {
        keyInfoLiveData.add(
            rxLiveDataTransformer.single(
                accountService.getKeysInfo()
            )
        )
    }

    fun deleteKeyInfo(fingerprintHex: String) {
        deleteKeysLiveData.add(
            rxLiveDataTransformer.single(
                accountService.deleteKeyInfo(fingerprintHex).andThen(Single.just(fingerprintHex))
            )
        )
    }

    fun deleteHDKey(fingerprintHex: String) {
        deleteKeysLiveData.add(
            rxLiveDataTransformer.single(
                accountService.deleteSeed(fingerprintHex).andThen(Single.just(fingerprintHex))
            )
        )
    }

    fun getSeed(fingerprint: String) {
        getSeedLiveData.add(rxLiveDataTransformer.single(
            accountService.getSeed(fingerprint)
        ))
    }

    fun updateKeyInfo(keyInfo: KeyInfo) {
        updateKeysLiveData.add(
            rxLiveDataTransformer.single(
                accountService.saveKeyInfo(keyInfo)
            )
        )
    }
}