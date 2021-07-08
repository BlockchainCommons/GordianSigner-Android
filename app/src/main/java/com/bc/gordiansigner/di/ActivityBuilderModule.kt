package com.bc.gordiansigner.di

import com.bc.gordiansigner.ui.account.AccountsActivity
import com.bc.gordiansigner.ui.account.AccountsModule
import com.bc.gordiansigner.ui.account.add_account.AddAccountActivity
import com.bc.gordiansigner.ui.account.add_account.AddAccountModule
import com.bc.gordiansigner.ui.account.contact.ContactsActivity
import com.bc.gordiansigner.ui.account.contact.ContactsModule
import com.bc.gordiansigner.ui.scan.QRScannerActivity
import com.bc.gordiansigner.ui.scan.QRScannerModule
import com.bc.gordiansigner.ui.share_account.ShareAccountMapActivity
import com.bc.gordiansigner.ui.share_account.ShareAccountMapModule
import com.bc.gordiansigner.ui.sign.PsbtSignActivity
import com.bc.gordiansigner.ui.sign.PsbtSignModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilderModule {

    @ContributesAndroidInjector(modules = [ShareAccountMapModule::class])
    @ActivityScope
    internal abstract fun bindShareAccountMapActivity(): ShareAccountMapActivity

    @ContributesAndroidInjector(modules = [AddAccountModule::class])
    @ActivityScope
    internal abstract fun bindAddAccountActivity(): AddAccountActivity

    @ContributesAndroidInjector(modules = [PsbtSignModule::class])
    @ActivityScope
    internal abstract fun bindPsbtSignActivity(): PsbtSignActivity

    @ContributesAndroidInjector(modules = [AccountsModule::class])
    @ActivityScope
    internal abstract fun bindAccountsActivity(): AccountsActivity

    @ContributesAndroidInjector(modules = [QRScannerModule::class])
    @ActivityScope
    internal abstract fun bindQRScannerActivity(): QRScannerActivity

    @ContributesAndroidInjector(modules = [ContactsModule::class])
    @ActivityScope
    internal abstract fun bindContactsActivity(): ContactsActivity
}