package com.kyberswap.android.util.di.module

import android.content.Context
import com.kyberswap.android.data.api.home.ChartApi
import com.kyberswap.android.data.api.home.CurrencyApi
import com.kyberswap.android.data.api.home.LimitOrderApi
import com.kyberswap.android.data.api.home.PromoApi
import com.kyberswap.android.data.api.home.SwapApi
import com.kyberswap.android.data.api.home.TokenApi
import com.kyberswap.android.data.api.home.TransactionApi
import com.kyberswap.android.data.api.home.UserApi
import com.kyberswap.android.data.api.home.UtilitiesApi
import com.kyberswap.android.data.db.AlertDao
import com.kyberswap.android.data.db.ContactDao
import com.kyberswap.android.data.db.LimitOrderDao
import com.kyberswap.android.data.db.LocalLimitOrderDao
import com.kyberswap.android.data.db.MarketDao
import com.kyberswap.android.data.db.OrderFilterDao
import com.kyberswap.android.data.db.PassCodeDao
import com.kyberswap.android.data.db.PendingBalancesDao
import com.kyberswap.android.data.db.RateDao
import com.kyberswap.android.data.db.RatingDao
import com.kyberswap.android.data.db.SelectedMarketDao
import com.kyberswap.android.data.db.SendDao
import com.kyberswap.android.data.db.SwapDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.db.TokenExtDao
import com.kyberswap.android.data.db.TransactionDao
import com.kyberswap.android.data.db.TransactionFilterDao
import com.kyberswap.android.data.db.UnitDao
import com.kyberswap.android.data.db.UserDao
import com.kyberswap.android.data.db.WalletDao
import com.kyberswap.android.data.mapper.AlertMapper
import com.kyberswap.android.data.mapper.CapMapper
import com.kyberswap.android.data.mapper.ChartMapper
import com.kyberswap.android.data.mapper.FeeMapper
import com.kyberswap.android.data.mapper.GasMapper
import com.kyberswap.android.data.mapper.OrderMapper
import com.kyberswap.android.data.mapper.PromoMapper
import com.kyberswap.android.data.mapper.RateMapper
import com.kyberswap.android.data.mapper.TokenMapper
import com.kyberswap.android.data.mapper.TransactionMapper
import com.kyberswap.android.data.mapper.UserMapper
import com.kyberswap.android.data.repository.AlertDataRepository
import com.kyberswap.android.data.repository.BalanceDataRepository
import com.kyberswap.android.data.repository.ContactDataRepository
import com.kyberswap.android.data.repository.LimitOrderDataRepository
import com.kyberswap.android.data.repository.MnemonicDataRepository
import com.kyberswap.android.data.repository.SettingDataRepository
import com.kyberswap.android.data.repository.SwapDataRepository
import com.kyberswap.android.data.repository.TokenDataRepository
import com.kyberswap.android.data.repository.TransactionDataRepository
import com.kyberswap.android.data.repository.UserDataRepository
import com.kyberswap.android.data.repository.WalletDataRepository
import com.kyberswap.android.data.repository.datasource.storage.StorageMediator
import com.kyberswap.android.domain.repository.AlertRepository
import com.kyberswap.android.domain.repository.BalanceRepository
import com.kyberswap.android.domain.repository.ContactRepository
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.repository.MnemonicRepository
import com.kyberswap.android.domain.repository.SettingRepository
import com.kyberswap.android.domain.repository.SwapRepository
import com.kyberswap.android.domain.repository.TokenRepository
import com.kyberswap.android.domain.repository.TransactionRepository
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.util.TokenClient
import com.trustwallet.walletconnect.WCClient
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
        promoMapper: PromoMapper,
        swapDao: SwapDao,
        sendDao: SendDao,
        limitOrderDao: LocalLimitOrderDao,
        wcClient: WCClient,
        tokenClient: TokenClient,
        transactionDao: TransactionDao,
        contactDao: ContactDao,
        userApi: UserApi
    ): WalletRepository =
        WalletDataRepository(
            context,
            walletDao,
            unitDao,
            tokenDao,
            promoApi,
            promoMapper,
            swapDao,
            sendDao,
            limitOrderDao,
            wcClient,
            tokenClient,
            transactionDao,
            contactDao,
            userApi
        )

    @Singleton
    @Provides
    @JvmStatic
    fun provideBalanceRepository(
        context: Context,
        api: TokenApi,
        currencyApi: CurrencyApi,
        tokenMapper: TokenMapper,
        client: TokenClient,
        tokenDao: TokenDao,
        tokenExtDao: TokenExtDao,
        walletDao: WalletDao,
        swapDao: SwapDao,
        sendDao: SendDao,
        localLimitOrderDao: LocalLimitOrderDao

    ): BalanceRepository =
        BalanceDataRepository(
            context,
            api,
            currencyApi,
            tokenMapper,
            client,
            tokenDao,
            tokenExtDao,
            walletDao,
            swapDao,
            sendDao,
            localLimitOrderDao
        )

    @Singleton
    @Provides
    @JvmStatic
    fun provideTokenRepository(
        client: TokenClient,
        tokenApi: TokenApi,
        api: SwapApi,
        chartApi: ChartApi,
        rateDao: RateDao,
        tokenDao: TokenDao,
        rateMapper: RateMapper,
        chartMapper: ChartMapper,
        context: Context
    ): TokenRepository =
        TokenDataRepository(
            client,
            tokenApi,
            api,
            chartApi,
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
        swapDao: SwapDao,
        tokenDao: TokenDao,
        sendDao: SendDao,
        contactDao: ContactDao,
        api: SwapApi,
        mapper: GasMapper,
        capMapper: CapMapper,
        tokenClient: TokenClient,
        transactionDao: TransactionDao,
        userDao: UserDao,
        userApi: UserApi,
        userMapper: UserMapper,
        utilitiesApi: UtilitiesApi,
        tokenExtDao: TokenExtDao
    ): SwapRepository =
        SwapDataRepository(
            context,
            swapDao,
            tokenDao,
            sendDao,
            contactDao,
            api,
            mapper,
            capMapper,
            tokenClient,
            transactionDao,
            userDao,
            userApi,
            userMapper,
            utilitiesApi,
            tokenExtDao
        )

    @Singleton
    @Provides
    @JvmStatic
    fun provideContactRepository(
        contactDao: ContactDao,
        sendDao: SendDao,
        context: Context
    ): ContactRepository =
        ContactDataRepository(contactDao, sendDao, context)

    @Singleton
    @Provides
    @JvmStatic
    fun provideTransactionRepository(
        api: TransactionApi,
        transactionDao: TransactionDao,
        mapper: TransactionMapper,
        tokenClient: TokenClient,
        tokenDao: TokenDao,
        swapDao: SwapDao,
        sendDao: SendDao,
        limitOrderDao: LocalLimitOrderDao,
        transactionFilterDao: TransactionFilterDao,
        userApi: UserApi,
        userDao: UserDao,
        context: Context
    ): TransactionRepository =
        TransactionDataRepository(
            api,
            transactionDao,
            mapper,
            tokenClient,
            tokenDao,
            swapDao,
            sendDao,
            userApi,
            limitOrderDao,
            transactionFilterDao,
            userDao,
            context
        )

    @Singleton
    @Provides
    @JvmStatic
    fun provideUserRepository(
        api: UserApi,
        userDao: UserDao,
        storageMediator: StorageMediator,
        userMapper: UserMapper,
        alertDao: AlertDao,
        ratingDao: RatingDao,
        context: Context
    ): UserRepository =
        UserDataRepository(api, userDao, storageMediator, userMapper, alertDao, ratingDao, context)

    @Singleton
    @Provides
    @JvmStatic
    fun provideLimitOrderRepository(
        context: Context,
        tokenDao: TokenDao,
        tokenExtDao: TokenExtDao,
        marketDao: MarketDao,
        selectedMarketDao: SelectedMarketDao,
        localLimitOrderDao: LocalLimitOrderDao,
        orderFilterDao: OrderFilterDao,
        dao: LimitOrderDao,
        pendingBalancesDao: PendingBalancesDao,
        api: LimitOrderApi,
        tokenApi: TokenApi,
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
            tokenExtDao,
            marketDao,
            selectedMarketDao,
            pendingBalancesDao,
            api,
            tokenApi,
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