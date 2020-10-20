package com.bc.gordiansigner.di

import com.bc.gordiansigner.service.AccountMapService
import com.bc.gordiansigner.service.TransactionService
import com.bc.gordiansigner.service.WalletService
import com.bc.gordiansigner.service.storage.file.FileStorageApi
import com.bc.gordiansigner.service.storage.sharedpref.SharedPrefApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ServiceModule {

    @Singleton
    @Provides
    fun provideAccountMapService(sharedPrefApi: SharedPrefApi, fileStorageApi: FileStorageApi) =
        AccountMapService(sharedPrefApi, fileStorageApi)

    @Singleton
    @Provides
    fun provideTransactionService(sharedPrefApi: SharedPrefApi, fileStorageApi: FileStorageApi) =
        TransactionService(sharedPrefApi, fileStorageApi)

    @Singleton
    @Provides
    fun provideWalletService(sharedPrefApi: SharedPrefApi, fileStorageApi: FileStorageApi) =
        WalletService(sharedPrefApi, fileStorageApi)

}