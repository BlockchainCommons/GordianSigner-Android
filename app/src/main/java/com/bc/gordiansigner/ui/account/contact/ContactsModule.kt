package com.bc.gordiansigner.ui.account.contact

import com.bc.gordiansigner.di.ActivityScope
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.service.ContactService
import com.bc.gordiansigner.ui.DialogController
import com.bc.gordiansigner.ui.Navigator
import dagger.Module
import dagger.Provides

@Module
class ContactsModule {

    @ActivityScope
    @Provides
    fun provideVM(
        activity: ContactsActivity,
        contactService: ContactService,
        rxLiveDataTransformer: RxLiveDataTransformer
    ) = ContactsViewModel(activity.lifecycle, contactService, rxLiveDataTransformer)

    @ActivityScope
    @Provides
    fun provideNavigator(activity: ContactsActivity) = Navigator(activity)

    @ActivityScope
    @Provides
    fun provideDialogController(activity: ContactsActivity) = DialogController(activity)
}