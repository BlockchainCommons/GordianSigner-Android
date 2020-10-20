package com.bc.gordiansigner.ui.sign

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.Error.NO_APPROPRIATE_HD_KEY_ERROR
import com.bc.gordiansigner.helper.Error.NO_HD_KEY_FOUND_ERROR
import com.bc.gordiansigner.helper.Error.PSBT_UNABLE_TO_SIGN_ERROR
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.model.Psbt
import com.bc.gordiansigner.service.TransactionService
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.ui.BaseViewModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class PsbtSignViewModel(
    lifecycle: Lifecycle,
    private val walletService: WalletService,
    private val transactionService: TransactionService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val psbtSigningLiveData = CompositeLiveData<String>()
    internal val psbtCheckingLiveData = CompositeLiveData<Any>()

    fun checkPsbt(base64: String) {
        psbtCheckingLiveData.add(rxLiveDataTransformer.completable(
            Completable.fromCallable {
                Psbt(base64)
            }
        ))
    }

    fun signPsbt(base64: String) {
        psbtSigningLiveData.add(rxLiveDataTransformer.single(Single.fromCallable {
            val psbt = Psbt(base64)
            if (!psbt.signable) {
                throw PSBT_UNABLE_TO_SIGN_ERROR
            }
            psbt
        }.subscribeOn(Schedulers.computation()).observeOn(Schedulers.io()).flatMap { psbt ->
            walletService.getHDKeyXprvs().flatMap { hdKeys ->
                if (hdKeys.isEmpty()) {
                    Single.error(NO_HD_KEY_FOUND_ERROR)
                } else {
                    val inputBip32DerivFingerprints =
                        psbt.inputBip32Derivs.map { it.fingerprintHex }
                    val hdKey =
                        hdKeys.firstOrNull() { inputBip32DerivFingerprints.contains(it.fingerprintHex) }
                    if (hdKey == null) {
                        Single.error(NO_APPROPRIATE_HD_KEY_ERROR)
                    } else {
                        val path =
                            psbt.inputBip32Derivs.first { it.fingerprintHex == hdKey.fingerprintHex }.path
                        transactionService.signPsbt(
                            psbt,
                            hdKey.derive(path)
                        )
                    }

                }
            }
        }
        ))
    }
}