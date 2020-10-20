package com.bc.gordiansigner

import com.bc.gordiansigner.di.DaggerAppComponent
import com.blockstream.libwally.Wally
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class GordianSignerApplication : DaggerApplication() {

    private val applicationInjector = DaggerAppComponent.builder()
        .application(this)
        .build()

    override fun onCreate() {
        super.onCreate()
        Wally.init(0)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = applicationInjector
}