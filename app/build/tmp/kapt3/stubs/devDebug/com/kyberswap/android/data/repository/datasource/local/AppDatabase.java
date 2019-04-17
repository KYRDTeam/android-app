package com.kyberswap.android.data.repository.datasource.local;

import java.lang.System;

@android.arch.persistence.room.TypeConverters(value = {com.kyberswap.android.data.repository.datasource.local.HeaderTypeConverter.class})
@android.arch.persistence.room.Database(entities = {com.kyberswap.android.data.api.home.entity.HeaderEntity.class}, version = 5)
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00052\u00020\u0001:\u0001\u0005B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&\u00a8\u0006\u0006"}, d2 = {"Lcom/kyberswap/android/data/repository/datasource/local/AppDatabase;", "Landroid/arch/persistence/room/RoomDatabase;", "()V", "articleFeatureDao", "Lcom/kyberswap/android/data/repository/datasource/local/HeaderDao;", "Companion", "app_devDebug"})
public abstract class AppDatabase extends android.arch.persistence.room.RoomDatabase {
    private static volatile com.kyberswap.android.data.repository.datasource.local.AppDatabase INSTANCE;
    public static final com.kyberswap.android.data.repository.datasource.local.AppDatabase.Companion Companion = null;
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.kyberswap.android.data.repository.datasource.local.HeaderDao articleFeatureDao();
    
    public AppDatabase() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007H\u0002J\u000e\u0010\b\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/kyberswap/android/data/repository/datasource/local/AppDatabase$Companion;", "", "()V", "INSTANCE", "Lcom/kyberswap/android/data/repository/datasource/local/AppDatabase;", "buildDatabase", "context", "Landroid/content/Context;", "getInstance", "app_devDebug"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final com.kyberswap.android.data.repository.datasource.local.AppDatabase getInstance(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;

        
        private final com.kyberswap.android.data.repository.datasource.local.AppDatabase buildDatabase(android.content.Context context) {
            return null;

        
        private Companion() {
            super();

    }
}