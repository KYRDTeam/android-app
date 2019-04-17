package com.kyberswap.android.domain;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\b\u0010\u0004\u001a\u00020\u0003H&\u00a8\u0006\u0005"}, d2 = {"Lcom/kyberswap/android/domain/SchedulerProvider;", "", "io", "Lio/reactivex/Scheduler;", "ui", "app_devDebug"})
public abstract interface SchedulerProvider {
    
    @org.jetbrains.annotations.NotNull()
    public abstract io.reactivex.Scheduler ui();
    
    @org.jetbrains.annotations.NotNull()
    public abstract io.reactivex.Scheduler io();
}