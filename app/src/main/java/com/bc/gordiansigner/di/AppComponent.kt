package com.bc.gordiansigner.di

import android.app.Application
import com.bc.gordiansigner.GordianSignerApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Component(
    modules = [AndroidSupportInjectionModule::class, AppModule::class,
        ActivityBuilderModule::class, FragmentBuilderModule::class, ServiceBuilderModule::class, ServiceModule::class]
)
@Singleton
interface AppComponent : AndroidInjector<GordianSignerApplication> {
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun build(): AppComponent

    }
}