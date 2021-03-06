package com.bc.gordiansigner.ui.account.add_account

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import dagger.Module
import dagger.Provides

@Module
class AddAccountModule {

    @ActivityScope
    @Provides
    fun provideVM(
        activity: AddAccountActivity,
        walletService: WalletService,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = AddAccountViewModel(activity.lifecycle, walletService, rxLiveDataTransformer)

    @ActivityScope
    @Provides
    fun provideNavigator(activity: AddAccountActivity) = Navigator(activity)

    @ActivityScope
    @Provides
    fun dialogController(activity: AddAccountActivity) = DialogController(activity)
}