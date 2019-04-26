package com.kyberswap.android.util.di.module

import android.app.Application
import android.content.Context
import com.kyberswap.android.domain.AppSchedulerProvider
import com.kyberswap.android.domain.SchedulerProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {

    @Singleton
    @Provides
    @JvmStatic
    fun provideContext(application: Application): Context = application

    @Singleton
    @Provides
    @JvmStatic
    fun provideSchedulerProvider(): SchedulerProvider = AppSchedulerProvider()

}
