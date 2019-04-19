package com.kyberswap.android.util.di.module

import android.app.Application
import android.content.Context
import com.kyberswap.android.Constant.BIP39_ENGLISH_SHA256
import com.kyberswap.android.domain.AppSchedulerProvider
import com.kyberswap.android.domain.SchedulerProvider
import dagger.Module
import dagger.Provides
import org.bitcoinj.crypto.MnemonicCode
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

    @Singleton
    @Provides
    @JvmStatic
    fun provideMnemonicCode(context: Context): MnemonicCode {
        val wis = context.assets.open("BIP39/en.txt")
        return MnemonicCode(wis, BIP39_ENGLISH_SHA256)
    }
}
