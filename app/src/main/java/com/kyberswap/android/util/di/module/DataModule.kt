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
        unitDao: UnitDao,
        tokenDao: TokenDao,
        promoApi: PromoApi,
        promoMapper: PromoMapper
    ): WalletRepository =
        WalletDataRepository(context, walletDao, unitDao, tokenDao, promoApi, promoMapper)

    @Singleton
    @Provides
    @JvmStatic
    fun provideBalanceRepository(
        api: TokenApi,
        currencyApi: CurrencyApi,
        tokenMapper: TokenMapper,
        client: TokenClient,
        tokenDao: TokenDao
    ): BalanceRepository =
        BalanceDataRepository(api, currencyApi, tokenMapper, client, tokenDao)

    @Singleton
    @Provides
    @JvmStatic
    fun provideTokenRepository(
        client: TokenClient,
        api: SwapApi,
        tokenApi: TokenApi,
        rateDao: RateDao,
        tokenDao: TokenDao,
        rateMapper: RateMapper,
        chartMapper: ChartMapper,
        context: Context
    ): TokenRepository =
        TokenDataRepository(
            client,
            api,
            tokenApi,
            rateDao,
            tokenDao,
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
        tokenClient: TokenClient,
        transactionDao: TransactionDao
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
            tokenClient,
            transactionDao
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
        mapper: TransactionMapper,
        tokenClient: TokenClient
    ): TransactionRepository =
        TransactionDataRepository(api, transactionDao, mapper, tokenClient)


    @Singleton
    @Provides
    @JvmStatic
    fun provideUserRepository(
        api: UserApi,
        userDao: UserDao,
        storageMediator: StorageMediator,
        userMapper: UserMapper,
        alertDao: AlertDao
    ): UserRepository =
        UserDataRepository(api, userDao, storageMediator, userMapper, alertDao)


    @Singleton
    @Provides
    @JvmStatic
    fun provideLimitOrderRepository(
        context: Context,
        tokenDao: TokenDao,
        localLimitOrderDao: LocalLimitOrderDao,
        orderFilterDao: OrderFilterDao,
        dao: LimitOrderDao,
        pendingBalancesDao: PendingBalancesDao,
        api: LimitOrderApi,
        mapper: OrderMapper,
        feeMapper: FeeMapper,
        tokenClient: TokenClient
    ): LimitOrderRepository =
        LimitOrderDataRepository(
            context,
            dao,
            localLimitOrderDao,
            orderFilterDao,
            tokenDao,
            pendingBalancesDao,
            api,
            tokenClient,
            mapper,
            feeMapper
        )


    @Singleton
    @Provides
    @JvmStatic
    fun provideAlertRepository(
        alertDao: AlertDao,
        tokenDao: TokenDao,
        userApi: UserApi,
        alertMapper: AlertMapper
    ): AlertRepository =
        AlertDataRepository(
            alertDao,
            tokenDao,
            userApi,
            alertMapper
        )

    @Singleton
    @Provides
    @JvmStatic
    fun providePassCodeRepository(
        passCodeDao: PassCodeDao
    ): SettingRepository =
        SettingDataRepository(
            passCodeDao
        )
}