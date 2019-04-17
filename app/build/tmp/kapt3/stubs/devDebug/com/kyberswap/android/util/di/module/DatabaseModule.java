package com.kyberswap.android.util.di.module;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0010\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\tH\u0007\u00a8\u0006\n"}, d2 = {"Lcom/kyberswap/android/util/di/module/DatabaseModule;", "", "()V", "provideArticleFeatureDao", "Lcom/kyberswap/android/data/repository/datasource/local/HeaderDao;", "db", "Lcom/kyberswap/android/data/repository/datasource/local/AppDatabase;", "provideDatabase", "app", "Landroid/app/Application;", "app_devDebug"})
@dagger.Module()
public final class DatabaseModule {
    
    @org.jetbrains.annotations.NotNull()
    @javax.inject.Singleton()
    @dagger.Provides()
    public final com.kyberswap.android.data.repository.datasource.local.AppDatabase provideDatabase(@org.jetbrains.annotations.NotNull()
    android.app.Application app) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @javax.inject.Singleton()
    @dagger.Provides()
    public final com.kyberswap.android.data.repository.datasource.local.HeaderDao provideArticleFeatureDao(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.repository.datasource.local.AppDatabase db) {
        return null;
    }
    
    public DatabaseModule() {
        super();
    }
}