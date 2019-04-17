package com.kyberswap.android.util.di

import com.kyberswap.android.data.api.home.HomeApi
import com.kyberswap.android.data.mapper.ArticleMapper
import com.kyberswap.android.data.mapper.HeaderMapper
import com.kyberswap.android.data.repository.FeatureArticleDataRepository
import com.kyberswap.android.data.repository.HeaderDataRepository
import com.kyberswap.android.data.repository.TopArticleDataRepository
import com.kyberswap.android.data.repository.datasource.HeaderDataStore
import com.kyberswap.android.domain.AppSchedulerProvider
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.repository.FeatureArticleRepository
import com.kyberswap.android.domain.repository.HeaderRepository
import com.kyberswap.android.domain.repository.TopArticleRepository
import com.kyberswap.android.util.di.qualifier.Local
import com.kyberswap.android.util.di.qualifier.Remote
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DomainModule {

    @Singleton @Provides @JvmStatic
    fun provideSchedulerProvider(): SchedulerProvider = AppSchedulerProvider()

    @Singleton @Provides @JvmStatic
    fun provideFeatureArticleRepository(
        homeApi: HomeApi,
        mapper: ArticleMapper
    ): FeatureArticleRepository = FeatureArticleDataRepository(homeApi, mapper)

    @Singleton @Provides @JvmStatic
    fun provideHeaderRepository(
            @Local local: HeaderDataStore,
            @Remote remote: HeaderDataStore,
            mapper: HeaderMapper
    ): HeaderRepository = HeaderDataRepository(local, remote, mapper)

    @Singleton @Provides @JvmStatic
    fun provideTopArticleRepository(
        homeApi: HomeApi,
        mapper: ArticleMapper
    ): TopArticleRepository = TopArticleDataRepository(homeApi, mapper)
}
