package com.kyberswap.android.util.di.module

import android.content.Context
import com.kyberswap.android.data.repository.GethKeystoreAccountService
import com.kyberswap.android.data.service.AccountService
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Singleton

@Module
object ServiceModule {
    @Singleton
    @Provides
    @JvmStatic
    fun provideAccountService(
        file: File
    ): AccountService = GethKeystoreAccountService(file)

    @Singleton
    @Provides
    @JvmStatic
    fun provideFile(
        context: Context
    ): File = File(context.filesDir, "keystore/keystore")

}