package com.kyberswap.android.util.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {

    @Singleton @Provides @JvmStatic
    fun provideContext(application: Application): Context = application
}
