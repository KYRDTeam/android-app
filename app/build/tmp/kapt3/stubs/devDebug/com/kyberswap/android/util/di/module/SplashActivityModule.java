package com.kyberswap.android.util.di.module;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\'J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tH\'\u00a8\u0006\n"}, d2 = {"Lcom/kyberswap/android/util/di/module/SplashActivityModule;", "", "bindSplashViewModel", "Landroid/arch/lifecycle/ViewModel;", "splashViewModel", "Lcom/kyberswap/android/presentation/splash/SplashViewModel;", "bindsAppCompatActivity", "Landroid/support/v7/app/AppCompatActivity;", "splashActivity", "Lcom/kyberswap/android/presentation/splash/SplashActivity;", "app_devDebug"})
@dagger.Module()
public abstract interface SplashActivityModule {
    
    @org.jetbrains.annotations.NotNull()
    @dagger.Binds()
    public abstract android.support.v7.app.AppCompatActivity bindsAppCompatActivity(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.presentation.splash.SplashActivity splashActivity);
    
    @org.jetbrains.annotations.NotNull()
    @com.kyberswap.android.util.di.ViewModelKey(value = com.kyberswap.android.presentation.splash.SplashViewModel.class)
    @dagger.multibindings.IntoMap()
    @dagger.Binds()
    public abstract android.arch.lifecycle.ViewModel bindSplashViewModel(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.presentation.splash.SplashViewModel splashViewModel);
}