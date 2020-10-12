package com.bc.gordiansigner.ui.share_account

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.AccountMapService
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.ui.BaseViewModel
import io.reactivex.Single

class ShareAccountMapViewModel(
    lifecycle: Lifecycle,
    private val walletService: WalletService,
    private val accountMapService: AccountMapService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val accountMapLiveData = CompositeLiveData<String>()
    internal val accountMapStatusLiveData = CompositeLiveData<Any>()

    fun checkValidAccountMap(string: String) {
        accountMapStatusLiveData.add(
            rxLiveDataTransformer.completable(
                accountMapService.getAccountMapInfo(string).ignoreElement()
            )
        )
    }

    fun updateAccountMap(accountMapString: String) {
        accountMapLiveData.add(rxLiveDataTransformer.single(
            accountMapService.getAccountMapInfo(accountMapString)
                .flatMap { (accountMap, descriptor) ->
                    walletService.getLocalHDKeyXprvs().flatMap { hdKeys ->
                        if (hdKeys.isNotEmpty()) {
                            accountMapService.fillPartialAccountMap(
                                accountMap,
                                descriptor,
                                hdKeys.first()
                            )
                        } else {
                            Single.error(Throwable("Missing account, you need to import an account first!"))
                        }
                    }
                }
        ))
    }
}