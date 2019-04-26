package com.kyberswap.android.util.di.module

import android.content.Context
import com.kyberswap.android.data.api.home.TokenApi
import com.kyberswap.android.data.mapper.TokenMapper
import com.kyberswap.android.data.repository.BalanceDataRepository
import com.kyberswap.android.data.repository.MnemonicDataRepository
import com.kyberswap.android.data.repository.TrustPasswordStore
import com.kyberswap.android.data.repository.WalletDataRepository
import com.kyberswap.android.domain.repository.BalanceRepository
import com.kyberswap.android.domain.repository.MnemonicRepository
import com.kyberswap.android.domain.repository.PasswordRepository
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.util.TokenClient
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
        context: Context
    ): WalletRepository = WalletDataRepository(context)

    @Singleton
    @Provides
    @JvmStatic
    fun provideBalanceRepository(
        api: TokenApi,
        tokenMapper: TokenMapper,
        client: TokenClient
    ): BalanceRepository = BalanceDataRepository(api, tokenMapper, client)

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