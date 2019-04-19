package com.kyberswap.android.util.di.module

import android.content.Context
import com.kyberswap.android.data.repository.MnemonicDataRepository
import com.kyberswap.android.data.repository.TrustPasswordStore
import com.kyberswap.android.data.repository.WalletDataRepository
import com.kyberswap.android.domain.repository.MnemonicRepository
import com.kyberswap.android.domain.repository.PasswordRepository
import com.kyberswap.android.domain.repository.WalletRepository
import dagger.Module
import dagger.Provides
import org.bitcoinj.crypto.MnemonicCode
import javax.inject.Singleton

@Module
object DataModule {
    @Singleton
    @Provides
    @JvmStatic
    fun provideWalletRepository(
    ): WalletRepository = WalletDataRepository()

    @Singleton
    @Provides
    @JvmStatic
    fun providePasswordRepository(context: Context): PasswordRepository =
        TrustPasswordStore(context)

    @Singleton
    @Provides
    @JvmStatic
    fun provideMnemonicRepository(mnemonicCode: MnemonicCode): MnemonicRepository =
        MnemonicDataRepository(mnemonicCode)
}