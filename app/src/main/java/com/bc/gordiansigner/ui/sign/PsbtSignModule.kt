package com.bc.gordiansigner.ui.sign

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.ContactService
import com.bc.gordiansigner.service.TransactionService
import com.bc.gordiansigner.service.AccountService
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
        accountService: AccountService,
        contactService: ContactService,
        transactionService: TransactionService,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = PsbtSignViewModel(
        activity.lifecycle,
        accountService,
        contactService,
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