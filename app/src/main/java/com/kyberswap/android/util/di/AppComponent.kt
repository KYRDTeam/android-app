package com.kyberswap.android.util.di

import android.app.Application
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.util.di.module.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ActivityBindingModule::class,
        AppModule::class,
        NetworkModule::class,
        DatabaseModule::class,
        DataModule::class
    ]
)
interface AppComponent : AndroidInjector<KyberSwapApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun networkModule(networkModule: NetworkModule): Builder
        fun databaseModule(databaseModule: DatabaseModule): Builder
        fun build(): AppComponent
    }

    override fun inject(instance: KyberSwapApplication)
}
