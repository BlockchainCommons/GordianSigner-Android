package com.bc.gordiansigner.ui.share_account

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.AccountMapService
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.ui.DialogController
import dagger.Module
import dagger.Provides

@Module
class ShareAccountMapModule {

    @Provides
    @ActivityScope
    fun provideVM(
        activity: ShareAccountMapActivity,
        walletService: WalletService,
        accountMapService: AccountMapService,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = ShareAccountMapViewModel(
        activity.lifecycle,
        walletService,
        accountMapService,
        rxLiveDataTransformer
    )

    @ActivityScope
    @Provides
    fun provideDialogController(activity: ShareAccountMapActivity) = DialogController(activity)
}