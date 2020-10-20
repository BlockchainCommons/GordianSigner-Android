package com.bc.gordiansigner.ui.account.add_account

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.Network
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.ui.BaseViewModel

class AddAccountViewModel(
    lifecycle: Lifecycle,
    private val walletService: WalletService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val importAccountLiveData = CompositeLiveData<Any>()

    fun importWallet(phrase: String, network: Network) {
        importAccountLiveData.add(
            rxLiveDataTransformer.completable(
                walletService.importHDKeyWallet(phrase, network).flatMap {
                    walletService.saveHDKeyXprv(it)
                }.ignoreElement()
            )
        )
    }
}