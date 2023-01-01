package com.bc.gordiansigner.ui.account.add_account

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.Error.FINGERPRINT_NOT_MATCH_ERROR
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.service.AccountService
import com.bc.gordiansigner.ui.BaseViewModel
import io.reactivex.Single

class AddAccountViewModel(
    lifecycle: Lifecycle,
    private val accountService: AccountService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val importAccountLiveData = CompositeLiveData<Pair<KeyInfo, String>>()
    internal val generateSignerLiveData = CompositeLiveData<String>()

    fun importWallet(phrase: String, alias: String, saveXpriv: Boolean, keyInfo: KeyInfo?) {
        importAccountLiveData.add(
            rxLiveDataTransformer.single(
                accountService.importMnemonic(phrase).flatMap { (seed, key) ->
                    val importedKeyInfo =
                        KeyInfo.newDefaultInstance(key.fingerprintHex, alias, saveXpriv)

                    if (keyInfo != null && keyInfo != importedKeyInfo) {
                        Single.error(FINGERPRINT_NOT_MATCH_ERROR)
                    } else {
                        accountService.saveSeedAndKeyInfo(importedKeyInfo, seed)
                            .map { Pair(importedKeyInfo, it) }
                    }
                }
            )
        )
    }

    fun generateSigner() {
        generateSignerLiveData.add(
            rxLiveDataTransformer.single(
                accountService.generateMnemonic()
            )
        )
    }
}