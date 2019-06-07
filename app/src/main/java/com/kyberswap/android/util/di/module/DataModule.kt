package com.kyberswap.android.util.di.module

import android.content.Context
import com.kyberswap.android.data.api.home.*
import com.kyberswap.android.data.db.*
import com.kyberswap.android.data.mapper.*
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
        swapDao: SwapDao,
        tokenApi: TokenApi,
        rateDao: RateDao,
        rateMapper: RateMapper,
        chartMapper: ChartMapper,
        context: Context
    ): TokenRepository =
        TokenDataRepository(
            client,
            api,
            swapDao,
            tokenApi,
            rateDao,
            rateMapper,
            chartMapper,
            context
        )

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
        walletDao: WalletDao,
        swapDao: SwapDao,
        tokenDao: TokenDao,
        sendDao: SendDao,
        contactDao: ContactDao,
        api: SwapApi,
        mapper: GasMapper,
        capMapper: CapMapper,
        tokenClient: TokenClient
    ): SwapRepository =
        SwapDataRepository(
            context,
            walletDao,
            swapDao,
            tokenDao,
            sendDao,
            contactDao,
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


    @Singleton
    @Provides
    @JvmStatic
    fun provideTransactionRepository(
        api: TransactionApi,
        transactionDao: TransactionDao,
        mapper: TransactionMapper
    ): TransactionRepository =
        TransactionDataRepository(api, transactionDao, mapper)


    @Singleton
    @Provides
    @JvmStatic
    fun provideUserRepository(
        api: UserApi,
        userDao: UserDao,
        storageMediator: StorageMediator,
        userMapper: UserMapper
    ): UserRepository =
        UserDataRepository(api, userDao, storageMediator, userMapper)


    @Singleton
    @Provides
    @JvmStatic
    fun provideLimitOrderRepository(
        tokenDao: TokenDao,
        localLimitOrderDao: LocalLimitOrderDao,
        dao: LimitOrderDao,
        api: LimitOrderApi,
        mapper: OrderMapper
    ): LimitOrderRepository =
        LimitOrderDataRepository(
            dao,
            localLimitOrderDao,
            tokenDao,
            api,
            mapper
        )
}