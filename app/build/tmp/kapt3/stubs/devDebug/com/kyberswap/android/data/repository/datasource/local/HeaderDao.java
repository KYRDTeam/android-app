package com.kyberswap.android.data.repository.datasource.local;

import java.lang.System;

@android.arch.persistence.room.Dao()
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0005\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004H\'J\b\u0010\n\u001a\u00020\bH\'J\u0010\u0010\u000b\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004H\'J\u0010\u0010\f\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004H\u0017R\u001a\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00038gX\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\r"}, d2 = {"Lcom/kyberswap/android/data/repository/datasource/local/HeaderDao;", "", "all", "Lio/reactivex/Flowable;", "Lcom/kyberswap/android/data/api/home/entity/HeaderEntity;", "getAll", "()Lio/reactivex/Flowable;", "delete", "", "headerEntity", "deleteAll", "insert", "updateData", "app_devDebug"})
public abstract interface HeaderDao {
    
    @org.jetbrains.annotations.NotNull()
    @android.arch.persistence.room.Query(value = "SELECT * FROM HeaderEntity")
    public abstract io.reactivex.Flowable<com.kyberswap.android.data.api.home.entity.HeaderEntity> getAll();
    
    @android.arch.persistence.room.Insert(onConflict = android.arch.persistence.room.OnConflictStrategy.REPLACE)
    public abstract void insert(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.entity.HeaderEntity headerEntity);
    
    @android.arch.persistence.room.Query(value = "DELETE FROM HeaderEntity")
    public abstract void deleteAll();
    
    @android.arch.persistence.room.Transaction()
    public abstract void updateData(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.entity.HeaderEntity headerEntity);
    
    @android.arch.persistence.room.Delete()
    public abstract void delete(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.entity.HeaderEntity headerEntity);
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 3)
    public final class DefaultImpls {
        
        @android.arch.persistence.room.Transaction()
        public static void updateData(com.kyberswap.android.data.repository.datasource.local.HeaderDao $this, @org.jetbrains.annotations.NotNull()
        com.kyberswap.android.data.api.home.entity.HeaderEntity headerEntity) {
        }
    }
}