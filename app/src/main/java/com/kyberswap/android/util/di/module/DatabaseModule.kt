package com.kyberswap.android.util.di.module

import android.app.Application
import com.kyberswap.android.data.db.AppDatabase
import com.kyberswap.android.data.db.TokenDao
import com.kyberswap.android.data.db.UnitDao
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

}
