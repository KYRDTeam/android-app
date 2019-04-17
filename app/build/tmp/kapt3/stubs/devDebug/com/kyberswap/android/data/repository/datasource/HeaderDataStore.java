package com.kyberswap.android.data.repository.datasource;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H&J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0004H&\u00a8\u0006\b"}, d2 = {"Lcom/kyberswap/android/data/repository/datasource/HeaderDataStore;", "", "headers", "Lio/reactivex/Flowable;", "Lcom/kyberswap/android/data/api/home/entity/HeaderEntity;", "save", "", "headerEntity", "app_devDebug"})
public abstract interface HeaderDataStore {
    
    @org.jetbrains.annotations.NotNull()
    public abstract io.reactivex.Flowable<com.kyberswap.android.data.api.home.entity.HeaderEntity> headers();
    
    public abstract void save(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.entity.HeaderEntity headerEntity);
}