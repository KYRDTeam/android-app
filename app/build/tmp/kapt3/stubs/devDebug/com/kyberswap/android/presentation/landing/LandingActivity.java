package com.kyberswap.android.presentation.landing;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u0000 \u001f2\u00020\u0001:\u0001\u001fB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u001b\u001a\u00020\u001c2\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u0014R#\u0010\u0003\u001a\n \u0005*\u0004\u0018\u00010\u00040\u00048BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\b\u0010\t\u001a\u0004\b\u0006\u0010\u0007R\u001e\u0010\n\u001a\u00020\u000b8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fR\u001b\u0010\u0010\u001a\u00020\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0014\u0010\t\u001a\u0004\b\u0012\u0010\u0013R\u001e\u0010\u0015\u001a\u00020\u00168\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001a\u00a8\u0006 "}, d2 = {"Lcom/kyberswap/android/presentation/landing/LandingActivity;", "Lcom/kyberswap/android/presentation/base/BaseActivity;", "()V", "binding", "Lcom/kyberswap/android/databinding/ActivityLandingBinding;", "kotlin.jvm.PlatformType", "getBinding", "()Lcom/kyberswap/android/databinding/ActivityLandingBinding;", "binding$delegate", "Lkotlin/Lazy;", "navigator", "Lcom/kyberswap/android/presentation/helper/Navigator;", "getNavigator", "()Lcom/kyberswap/android/presentation/helper/Navigator;", "setNavigator", "(Lcom/kyberswap/android/presentation/helper/Navigator;)V", "viewModel", "Lcom/kyberswap/android/presentation/landing/LandingViewModel;", "getViewModel", "()Lcom/kyberswap/android/presentation/landing/LandingViewModel;", "viewModel$delegate", "viewModelFactory", "Lcom/kyberswap/android/util/di/ViewModelFactory;", "getViewModelFactory", "()Lcom/kyberswap/android/util/di/ViewModelFactory;", "setViewModelFactory", "(Lcom/kyberswap/android/util/di/ViewModelFactory;)V", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "Companion", "app_devDebug"})
public final class LandingActivity extends com.kyberswap.android.presentation.base.BaseActivity {
    @org.jetbrains.annotations.NotNull()
    @javax.inject.Inject()
    public com.kyberswap.android.util.di.ViewModelFactory viewModelFactory;
    @org.jetbrains.annotations.NotNull()
    @javax.inject.Inject()
    public com.kyberswap.android.presentation.helper.Navigator navigator;
    private final kotlin.Lazy viewModel$delegate = null;
    private final kotlin.Lazy binding$delegate = null;
    public static final com.kyberswap.android.presentation.landing.LandingActivity.Companion Companion = null;
    private java.util.HashMap _$_findViewCache;
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.util.di.ViewModelFactory getViewModelFactory() {
        return null;
    }
    
    public final void setViewModelFactory(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.util.di.ViewModelFactory p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.presentation.helper.Navigator getNavigator() {
        return null;
    }
    
    public final void setNavigator(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.presentation.helper.Navigator p0) {
    }
    
    private final com.kyberswap.android.presentation.landing.LandingViewModel getViewModel() {
        return null;
    }
    
    private final com.kyberswap.android.databinding.ActivityLandingBinding getBinding() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    public LandingActivity() {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/kyberswap/android/presentation/landing/LandingActivity$Companion;", "", "()V", "newIntent", "Landroid/content/Intent;", "context", "Landroid/content/Context;", "app_devDebug"})
    public static final class Companion {
        
        @org.jetbrains.annotations.NotNull()
        public final android.content.Intent newIntent(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
        
        private Companion() {
            super();
        }
    }
}