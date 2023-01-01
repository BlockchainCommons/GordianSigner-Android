package com.bc.gordiansigner.ui.account

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.AccountService
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import dagger.Module
import dagger.Provides

@Module
class AccountsModule {

    @ActivityScope
    @Provides
    fun provideVM(
        activity: AccountsActivity,
        accountService: AccountService,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = AccountsViewModel(activity.lifecycle, accountService, rxLiveDataTransformer)

    @ActivityScope
    @Provides
    fun provideNavigator(activity: AccountsActivity) = Navigator(activity)

    @ActivityScope
    @Provides
    fun provideDialogController(activity: AccountsActivity) = DialogController(activity)
}