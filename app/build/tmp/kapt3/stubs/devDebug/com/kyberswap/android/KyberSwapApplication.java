package com.kyberswap.android;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u00012\u00020\u0002B\u0005\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00010\u0005H\u0014J\b\u0010\u0006\u001a\u00020\u0007H\u0007J\b\u0010\b\u001a\u00020\u0007H\u0007J\b\u0010\t\u001a\u00020\u0007H\u0016\u00a8\u0006\n"}, d2 = {"Lcom/kyberswap/android/KyberSwapApplication;", "Ldagger/android/support/DaggerApplication;", "Landroid/arch/lifecycle/LifecycleObserver;", "()V", "applicationInjector", "Ldagger/android/AndroidInjector;", "onAppBackgrounded", "", "onAppForegrounded", "onCreate", "app_devDebug"})
public final class KyberSwapApplication extends dagger.android.support.DaggerApplication implements android.arch.lifecycle.LifecycleObserver {
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    protected dagger.android.AndroidInjector<? extends dagger.android.support.DaggerApplication> applicationInjector() {
        return null;
    }
    
    @android.arch.lifecycle.OnLifecycleEvent(value = android.arch.lifecycle.Lifecycle.Event.ON_STOP)
    public final void onAppBackgrounded() {
    }
    
    @android.arch.lifecycle.OnLifecycleEvent(value = android.arch.lifecycle.Lifecycle.Event.ON_START)
    public final void onAppForegrounded() {
    }
    
    public KyberSwapApplication() {
        super();
    }
}