package com.kyberswap.android.util.di.module

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AnalyticModule {
    @Singleton
    @Provides
    @JvmStatic
    fun provideFirebaseAnalytics(
        context: Context
    ): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }
}