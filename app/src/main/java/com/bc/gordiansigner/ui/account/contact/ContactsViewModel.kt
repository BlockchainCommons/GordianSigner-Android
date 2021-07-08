package com.bc.gordiansigner.ui.account.contact

import androidx.lifecycle.Lifecycle
import com.bc.gordiansigner.helper.livedata.CompositeLiveData
import com.bc.gordiansigner.helper.livedata.RxLiveDataTransformer
import com.bc.gordiansigner.model.KeyInfo
import com.bc.gordiansigner.service.ContactService
import com.bc.gordiansigner.ui.BaseViewModel

class ContactsViewModel(
    lifecycle: Lifecycle,
    private val contactService: ContactService,
    private val rxLiveDataTransformer: RxLiveDataTransformer
) : BaseViewModel(lifecycle) {

    internal val contactsLiveData = CompositeLiveData<List<KeyInfo>>()
    internal val saveContactLiveData = CompositeLiveData<Pair<KeyInfo, Boolean>>()
    internal val deleteContactLiveData = CompositeLiveData<Any>()

    fun fetchContacts() {
        contactsLiveData.add(
            rxLiveDataTransformer.single(
                contactService.getContactKeysInfo()
            )
        )
    }

    fun saveContact(keyInfo: KeyInfo) {
        saveContactLiveData.add(
            rxLiveDataTransformer.single(
                contactService.saveContactKeyInfo(keyInfo)
            )
        )
    }

    fun deleteContact(keyInfo: KeyInfo) {
        deleteContactLiveData.add(
            rxLiveDataTransformer.completable(
                contactService.deleteContactKeyInfo(keyInfo.fingerprint)
            )
        )
    }
}