package com.kyberswap.android.data.repository.datasource.remote;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u0016J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0007H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/kyberswap/android/data/repository/datasource/remote/HeaderRemoteDataSource;", "Lcom/kyberswap/android/data/repository/datasource/HeaderDataStore;", "homeApi", "Lcom/kyberswap/android/data/api/home/HomeApi;", "(Lcom/kyberswap/android/data/api/home/HomeApi;)V", "headers", "Lio/reactivex/Flowable;", "Lcom/kyberswap/android/data/api/home/entity/HeaderEntity;", "save", "", "headerEntity", "app_devDebug"})
public final class HeaderRemoteDataSource implements com.kyberswap.android.data.repository.datasource.HeaderDataStore {
    private final com.kyberswap.android.data.api.home.HomeApi homeApi = null;
    
    @java.lang.Override()
    public void save(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.entity.HeaderEntity headerEntity) {
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public io.reactivex.Flowable<com.kyberswap.android.data.api.home.entity.HeaderEntity> headers() {
        return null;
    }
    
    @javax.inject.Inject()
    public HeaderRemoteDataSource(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.HomeApi homeApi) {
        super();
    }
}