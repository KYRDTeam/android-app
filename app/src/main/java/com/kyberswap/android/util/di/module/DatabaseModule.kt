package com.kyberswap.android.util.di.module

import android.app.Application
import com.kyberswap.android.data.repository.datasource.local.AppDatabase
import com.kyberswap.android.data.repository.datasource.local.HeaderDao
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
    fun provideArticleFeatureDao(db: AppDatabase): HeaderDao = db.articleFeatureDao()

}
