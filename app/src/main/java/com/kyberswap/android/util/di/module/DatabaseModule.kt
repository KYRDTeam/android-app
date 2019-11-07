package com.kyberswap.android.util.di.module

import android.app.Application
import com.kyberswap.android.data.db.AlertDao
import com.kyberswap.android.data.db.AppDatabase
import com.kyberswap.android.data.db.ContactDao
import com.kyberswap.android.data.db.LimitOrderDao
import com.kyberswap.android.data.db.LocalLimitOrderDao
import com.kyberswap.android.data.db.OrderFilterDao
import com.kyberswap.android.data.db.PassCodeDao
import com.kyberswap.android.data.db.PendingBalancesDao
import com.kyberswap.android.data.db.RateDao
import com.kyberswap.android.data.db.SendDao
import com.kyberswap.android.data.db.SwapDao
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.db.TokenExtDao
import com.kyberswap.android.data.db.TransactionDao
import com.kyberswap.android.data.db.TransactionFilterDao
import com.kyberswap.android.data.db.UnitDao
import com.kyberswap.android.data.db.UserDao
import com.kyberswap.android.data.db.WalletDao
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
        return appDatabase.tokenDao()
    }

    @Provides
    @Singleton
    fun provideTokenExtDao(appDatabase: AppDatabase): TokenExtDao {
        return appDatabase.tokenExtDao()
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

    @Provides
    @Singleton
    fun providePendingBalancesDao(appDatabase: AppDatabase): PendingBalancesDao {
        return appDatabase.pendingBalancesDao()
    }

    @Provides
    @Singleton
    fun provideTransactionFilterDao(appDatabase: AppDatabase): TransactionFilterDao {
        return appDatabase.transactionFilterDao()
    }
}
