package com.kyberswap.android.util.di.module

import android.content.Context
import com.kyberswap.android.data.api.home.RateApi
import com.kyberswap.android.data.api.home.TokenApi
import com.kyberswap.android.data.db.*
import com.kyberswap.android.data.mapper.RateMapper
import com.kyberswap.android.data.mapper.TokenMapper
import com.kyberswap.android.data.repository.*
import com.kyberswap.android.data.repository.datasource.storage.StorageMediator
import com.kyberswap.android.domain.repository.*
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
        context: Context,
        walletDao: WalletDao,
        unitDao: UnitDao,
        mediator: StorageMediator
    ): WalletRepository = WalletDataRepository(context, walletDao, unitDao, mediator)

    @Singleton
    @Provides
    @JvmStatic
    fun provideBalanceRepository(
        api: TokenApi,
        tokenMapper: TokenMapper,
        client: TokenClient,
        tokenDao: TokenDao,
        walletTokenDao: WalletTokenDao
    ): BalanceRepository = BalanceDataRepository(api, tokenMapper, client, tokenDao, walletTokenDao)

    @Singleton
    @Provides
    @JvmStatic
    fun provideTokenRepository(
        client: TokenClient,
        api: RateApi,
        rateMapper: RateMapper,
        rateDao: RateDao,
        context: Context
    ): TokenRepository = TokenDataRepository(client, api, rateMapper, rateDao, context)

    @Singleton
    @Provides
    @JvmStatic
    fun provideMnemonicRepository(mnemonicCode: MnemonicCode): MnemonicRepository =
        MnemonicDataRepository(mnemonicCode)

    @Singleton
    @Provides
    @JvmStatic
    fun provideSwapRepository(
        swapDao: SwapDao,
        tokenDao: TokenDao
    ): SwapRepository =
        SwapDataRepository(swapDao, tokenDao)
}