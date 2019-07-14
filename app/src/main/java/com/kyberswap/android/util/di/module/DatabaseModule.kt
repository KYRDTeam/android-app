package com.kyberswap.android.util.di.module

import android.app.Application
import com.kyberswap.android.data.db.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application) = AppDatabase.getInstance(app)

    @Provides
    @Singleton
    fun provideTokenDao(appDatabase: AppDatabase): TokenDao {
        return appDatabase.customerDao()
    }

    @Provides
    @Singleton
    fun provideWalletDao(appDatabase: AppDatabase): WalletDao {
        return appDatabase.walletDao()
    }

    @Provides
    @Singleton
    fun provideUnitDao(appDatabase: AppDatabase): UnitDao {
        return appDatabase.unitDao()
    }

    @Provides
    @Singleton
    fun provideSwapDao(appDatabase: AppDatabase): SwapDao {
        return appDatabase.swapDao()
    }

    @Provides
    @Singleton
    fun provideSendDao(appDatabase: AppDatabase): SendDao {
        return appDatabase.sendDao()
    }

    @Provides
    @Singleton
    fun provideWalletTokenDao(appDatabase: AppDatabase): WalletTokenDao {
        return appDatabase.walletTokenDao()
    }

    @Provides
    @Singleton
    fun provideRateDao(appDatabase: AppDatabase): RateDao {
        return appDatabase.rateDao()
    }

    @Provides
    @Singleton
    fun provideContactDao(appDatabase: AppDatabase): ContactDao {
        return appDatabase.contactDao()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao()
    }


    @Provides
    @Singleton
    fun provideOrderDao(appDatabase: AppDatabase): LimitOrderDao {
        return appDatabase.limitOrderDao()
    }

    @Provides
    @Singleton
    fun provideUserrDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }

    @Provides
    @Singleton
    fun provideLocalLimitOrderDao(appDatabase: AppDatabase): LocalLimitOrderDao {
        return appDatabase.localLimitOrderDao()
    }

    @Provides
    @Singleton
    fun provideOrderFilterDao(appDatabase: AppDatabase): OrderFilterDao {
        return appDatabase.orderFilterDao()
    }

    @Provides
    @Singleton
    fun provideAlertDao(appDatabase: AppDatabase): AlertDao {
        return appDatabase.alertDao()
    }

    @Provides
    @Singleton
    fun providePassCodeDao(appDatabase: AppDatabase): PassCodeDao {
        return appDatabase.passCodeDao()
    }

}
