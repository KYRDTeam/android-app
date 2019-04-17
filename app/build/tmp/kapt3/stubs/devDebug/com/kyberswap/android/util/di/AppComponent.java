package com.kyberswap.android.util.di;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\bg\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u0006J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0002H&\u00a8\u0006\u0007"}, d2 = {"Lcom/kyberswap/android/util/di/AppComponent;", "Ldagger/android/AndroidInjector;", "Lcom/kyberswap/android/KyberSwapApplication;", "inject", "", "instance", "Builder", "app_devDebug"})
@dagger.Component(modules = {dagger.android.support.AndroidSupportInjectionModule.class, com.kyberswap.android.util.di.module.ActivityBindingModule.class, com.kyberswap.android.util.di.module.AppModule.class, com.kyberswap.android.util.di.module.NetworkModule.class, com.kyberswap.android.util.di.module.DatabaseModule.class, com.kyberswap.android.util.di.module.DataModule.class})
@javax.inject.Singleton()
public abstract interface AppComponent extends dagger.android.AndroidInjector<com.kyberswap.android.KyberSwapApplication> {
    
    @java.lang.Override()
    public abstract void inject(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.KyberSwapApplication instance);
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00002\u0006\u0010\u0002\u001a\u00020\u0003H\'J\b\u0010\u0004\u001a\u00020\u0005H&J\u0010\u0010\u0006\u001a\u00020\u00002\u0006\u0010\u0006\u001a\u00020\u0007H&J\u0010\u0010\b\u001a\u00020\u00002\u0006\u0010\b\u001a\u00020\tH&\u00a8\u0006\n"}, d2 = {"Lcom/kyberswap/android/util/di/AppComponent$Builder;", "", "application", "Landroid/app/Application;", "build", "Lcom/kyberswap/android/util/di/AppComponent;", "databaseModule", "Lcom/kyberswap/android/util/di/module/DatabaseModule;", "networkModule", "Lcom/kyberswap/android/util/di/module/NetworkModule;", "app_devDebug"})
    @dagger.Component.Builder()
    public static abstract interface Builder {
        
        @org.jetbrains.annotations.NotNull()
        @dagger.BindsInstance()
        public abstract com.kyberswap.android.util.di.AppComponent.Builder application(@org.jetbrains.annotations.NotNull()
        android.app.Application application);
        
        @org.jetbrains.annotations.NotNull()
        public abstract com.kyberswap.android.util.di.AppComponent.Builder networkModule(@org.jetbrains.annotations.NotNull()
        com.kyberswap.android.util.di.module.NetworkModule networkModule);
        
        @org.jetbrains.annotations.NotNull()
        public abstract com.kyberswap.android.util.di.AppComponent.Builder databaseModule(@org.jetbrains.annotations.NotNull()
        com.kyberswap.android.util.di.module.DatabaseModule databaseModule);
        
        @org.jetbrains.annotations.NotNull()
        public abstract com.kyberswap.android.util.di.AppComponent build();
    }
}