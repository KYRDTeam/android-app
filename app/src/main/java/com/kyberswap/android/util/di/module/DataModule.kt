package com.kyberswap.android.util.di.module

import android.content.Context
import com.kyberswap.android.data.api.home.CurrencyApi
import com.kyberswap.android.data.api.home.SwapApi
import com.kyberswap.android.data.api.home.TokenApi
import com.kyberswap.android.data.db.*
import com.kyberswap.android.data.mapper.*
import com.kyberswap.android.data.repository.*
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
        unitDao: UnitDao
    ): WalletRepository = WalletDataRepository(context, walletDao, unitDao)

    @Singleton
    @Provides
    @JvmStatic
    fun provideBalanceRepository(
        api: TokenApi,
        currencyApi: CurrencyApi,
        tokenMapper: TokenMapper,
        client: TokenClient,
        tokenDao: TokenDao,
        walletTokenDao: WalletTokenDao
    ): BalanceRepository =
        BalanceDataRepository(api, currencyApi, tokenMapper, client, tokenDao, walletTokenDao)

    @Singleton
    @Provides
    @JvmStatic
    fun provideTokenRepository(
        client: TokenClient,
        api: SwapApi,
        tokenApi: TokenApi,
        rateDao: RateDao,
        rateMapper: RateMapper,
        chartMapper: ChartMapper,
        context: Context
    ): TokenRepository =
        TokenDataRepository(client, api, tokenApi, rateDao, rateMapper, chartMapper, context)

    @Singleton
    @Provides
    @JvmStatic
    fun provideMnemonicRepository(mnemonicCode: MnemonicCode): MnemonicRepository =
        MnemonicDataRepository(mnemonicCode)

    @Singleton
    @Provides
    @JvmStatic
    fun provideSwapRepository(
        context: Context,
        swapDao: SwapDao,
        tokenDao: TokenDao,
        sendDao: SendDao,
        api: SwapApi,
        mapper: GasMapper,
        capMapper: CapMapper,
        tokenClient: TokenClient
    ): SwapRepository =
        SwapDataRepository(
            context,
            swapDao,
            tokenDao,
            sendDao,
            api,
            mapper,
            capMapper,
            tokenClient
        )

    @Singleton
    @Provides
    @JvmStatic
    fun provideContactRepository(
        contactDao: ContactDao
    ): ContactRepository =
        ContactDataRepository(contactDao)
}