package com.kyberswap.android.util.di

import com.kyberswap.android.data.api.home.HomeApi
import com.kyberswap.android.data.repository.datasource.HeaderDataStore
import com.kyberswap.android.data.repository.datasource.local.HeaderDao
import com.kyberswap.android.data.repository.datasource.local.HeaderLocalDataSource
import com.kyberswap.android.data.repository.datasource.remote.HeaderRemoteDataSource
import com.kyberswap.android.util.di.qualifier.Local
import com.kyberswap.android.util.di.qualifier.Remote
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataSourceRepositoryModule {

    @Provides @Singleton @Local
    fun provideLocalDataSource(headerDao: HeaderDao): HeaderDataStore =
            HeaderLocalDataSource(headerDao)

    @Provides @Singleton @Remote
    fun provideRemoteDataSource(homeApi: HomeApi): HeaderDataStore =
            HeaderRemoteDataSource(homeApi)
}
