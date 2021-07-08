package com.bc.gordiansigner.ui.share_account

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.AccountMapService
import com.bc.gordiansigner.service.ContactService
import com.bc.gordiansigner.service.AccountService
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import dagger.Module
import dagger.Provides

@Module
class ShareAccountMapModule {

    @Provides
    @ActivityScope
    fun provideVM(
        activity: ShareAccountMapActivity,
        accountMapService: AccountMapService,
        contactService: ContactService,
        accountService: AccountService,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = ShareAccountMapViewModel(
        activity.lifecycle,
        accountMapService,
        contactService,
        accountService,
        rxLiveDataTransformer
    )

    @ActivityScope
    @Provides
    fun provideNavigator(activity: ShareAccountMapActivity) = Navigator(activity)

    @ActivityScope
    @Provides
    fun provideDialogController(activity: ShareAccountMapActivity) = DialogController(activity)
}