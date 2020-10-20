package com.bc.gordiansigner.ui.sign

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.TransactionService
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import dagger.Module
import dagger.Provides

@Module
class PsbtSignModule {

    @ActivityScope
    @Provides
    fun provideViewModel(
        activity: PsbtSignActivity,
        walletService: WalletService,
        transactionService: TransactionService,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = PsbtSignViewModel(
        activity.lifecycle,
        walletService,
        transactionService,
        rxLiveDataTransformer
    )

    @ActivityScope
    @Provides
    fun provideNavigator(activity: PsbtSignActivity) = Navigator(activity)

    @ActivityScope
    @Provides
    fun provideDialogController(activity: PsbtSignActivity) = DialogController(activity)
}