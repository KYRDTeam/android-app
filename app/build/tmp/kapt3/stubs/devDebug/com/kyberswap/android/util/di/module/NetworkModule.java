package com.kyberswap.android.util.di.module;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J1\u0010\u0003\u001a\u0002H\u0004\"\u0004\b\u0000\u0010\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u0002H\u00040\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0002\u00a2\u0006\u0002\u0010\u000bJ\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\t\u001a\u00020\nH\u0007J\u0010\u0010\u0010\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\u0012H\u0007\u00a8\u0006\u0013"}, d2 = {"Lcom/kyberswap/android/util/di/module/NetworkModule;", "", "()V", "createApiClient", "T", "clazz", "Ljava/lang/Class;", "baseUrl", "", "client", "Lokhttp3/OkHttpClient;", "(Ljava/lang/Class;Ljava/lang/String;Lokhttp3/OkHttpClient;)Ljava/lang/Object;", "provideHomeApi", "Lcom/kyberswap/android/data/api/home/HomeApi;", "context", "Landroid/content/Context;", "provideOkHttpClient", "storageMediator", "Lcom/kyberswap/android/data/repository/datasource/storage/StorageMediator;", "app_devDebug"})
@dagger.Module()
public final class NetworkModule {
    
    @org.jetbrains.annotations.NotNull()
    @javax.inject.Singleton()
    @dagger.Provides()
    public final okhttp3.OkHttpClient provideOkHttpClient(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.repository.datasource.storage.StorageMediator storageMediator) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @javax.inject.Singleton()
    @dagger.Provides()
    public final com.kyberswap.android.data.api.home.HomeApi provideHomeApi(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient client) {
        return null;
    }
    
    private final <T extends java.lang.Object>T createApiClient(java.lang.Class<T> clazz, java.lang.String baseUrl, okhttp3.OkHttpClient client) {
        return null;
    }
    
    public NetworkModule() {
        super();
    }
}