package com.kyberswap.android.util.di.module;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0007J$\u0010\t\u001a\u00020\n2\b\b\u0001\u0010\u000b\u001a\u00020\f2\b\b\u0001\u0010\r\u001a\u00020\f2\u0006\u0010\u0007\u001a\u00020\u000eH\u0007J\u0010\u0010\u000f\u001a\u00020\f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0007J\u0010\u0010\u0012\u001a\u00020\f2\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0018\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0007\u00a8\u0006\u0015"}, d2 = {"Lcom/kyberswap/android/util/di/module/DataModule;", "", "()V", "provideFeatureArticleRepository", "Lcom/kyberswap/android/domain/repository/FeatureArticleRepository;", "homeApi", "Lcom/kyberswap/android/data/api/home/HomeApi;", "mapper", "Lcom/kyberswap/android/data/mapper/ArticleMapper;", "provideHeaderRepository", "Lcom/kyberswap/android/domain/repository/HeaderRepository;", "local", "Lcom/kyberswap/android/data/repository/datasource/HeaderDataStore;", "remote", "Lcom/kyberswap/android/data/mapper/HeaderMapper;", "provideLocalDataSource", "headerDao", "Lcom/kyberswap/android/data/repository/datasource/local/HeaderDao;", "provideRemoteDataSource", "provideTopArticleRepository", "Lcom/kyberswap/android/domain/repository/TopArticleRepository;", "app_devDebug"})
@dagger.Module()
public final class DataModule {
    public static final com.kyberswap.android.util.di.module.DataModule INSTANCE = null;
    
    @org.jetbrains.annotations.NotNull()
    @dagger.Provides()
    @javax.inject.Singleton()
    public static final com.kyberswap.android.domain.repository.FeatureArticleRepository provideFeatureArticleRepository(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.HomeApi homeApi, @org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.mapper.ArticleMapper mapper) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @dagger.Provides()
    @javax.inject.Singleton()
    public static final com.kyberswap.android.domain.repository.HeaderRepository provideHeaderRepository(@org.jetbrains.annotations.NotNull()
    @com.kyberswap.android.util.di.qualifier.Local()
    com.kyberswap.android.data.repository.datasource.HeaderDataStore local, @org.jetbrains.annotations.NotNull()
    @com.kyberswap.android.util.di.qualifier.Remote()
    com.kyberswap.android.data.repository.datasource.HeaderDataStore remote, @org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.mapper.HeaderMapper mapper) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @dagger.Provides()
    @javax.inject.Singleton()
    public static final com.kyberswap.android.domain.repository.TopArticleRepository provideTopArticleRepository(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.HomeApi homeApi, @org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.mapper.ArticleMapper mapper) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @com.kyberswap.android.util.di.qualifier.Local()
    @javax.inject.Singleton()
    @dagger.Provides()
    public static final com.kyberswap.android.data.repository.datasource.HeaderDataStore provideLocalDataSource(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.repository.datasource.local.HeaderDao headerDao) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @com.kyberswap.android.util.di.qualifier.Remote()
    @javax.inject.Singleton()
    @dagger.Provides()
    public static final com.kyberswap.android.data.repository.datasource.HeaderDataStore provideRemoteDataSource(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.HomeApi homeApi) {
        return null;
    }
    
    private DataModule() {
        super();
    }
}