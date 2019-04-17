package com.kyberswap.android.util.di.module;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H\'J\b\u0010\u0004\u001a\u00020\u0005H\'J\b\u0010\u0006\u001a\u00020\u0007H\'\u00a8\u0006\b"}, d2 = {"Lcom/kyberswap/android/util/di/module/ActivityBindingModule;", "", "contributeLandingActivity", "Lcom/kyberswap/android/presentation/landing/LandingActivity;", "contributeMainActivity", "Lcom/kyberswap/android/presentation/main/MainActivity;", "contributeSplashActivity", "Lcom/kyberswap/android/presentation/splash/SplashActivity;", "app_devDebug"})
@dagger.Module()
public abstract interface ActivityBindingModule {
    
    @org.jetbrains.annotations.NotNull()
    @dagger.android.ContributesAndroidInjector(modules = {com.kyberswap.android.util.di.module.MainActivityModule.class})
    @com.kyberswap.android.util.di.scope.ActivityScoped()
    public abstract com.kyberswap.android.presentation.main.MainActivity contributeMainActivity();
    
    @org.jetbrains.annotations.NotNull()
    @dagger.android.ContributesAndroidInjector(modules = {com.kyberswap.android.util.di.module.SplashActivityModule.class})
    @com.kyberswap.android.util.di.scope.ActivityScoped()
    public abstract com.kyberswap.android.presentation.splash.SplashActivity contributeSplashActivity();
    
    @org.jetbrains.annotations.NotNull()
    @dagger.android.ContributesAndroidInjector(modules = {com.kyberswap.android.util.di.module.LandingActivityModule.class})
    @com.kyberswap.android.util.di.scope.ActivityScoped()
    public abstract com.kyberswap.android.presentation.landing.LandingActivity contributeLandingActivity();
}