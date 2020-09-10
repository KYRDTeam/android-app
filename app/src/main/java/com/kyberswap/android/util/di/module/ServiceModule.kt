package com.kyberswap.android.util.di.module

import com.kyberswap.android.presentation.main.walletconnect.service.WcSessionManagerService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun bindService(): WcSessionManagerService

}