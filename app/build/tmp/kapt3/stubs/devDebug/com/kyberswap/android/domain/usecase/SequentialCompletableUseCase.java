package com.kyberswap.android.domain.usecase;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0003\n\u0002\b\u0002\b&\u0018\u0000*\u0006\b\u0000\u0010\u0001 \u00002\u00020\u0002B\u000f\b\u0004\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\u0002\u0010\u0005J\u0015\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00028\u0000H$\u00a2\u0006\u0002\u0010\u000bJ\u0006\u0010\f\u001a\u00020\rJ)\u0010\u000e\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\u00102\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00130\u00122\u0006\u0010\n\u001a\u00028\u0000\u00a2\u0006\u0002\u0010\u0014R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/kyberswap/android/domain/usecase/SequentialCompletableUseCase;", "PARAM", "", "schedulerProvider", "Lcom/kyberswap/android/domain/SchedulerProvider;", "(Lcom/kyberswap/android/domain/SchedulerProvider;)V", "compositeDisposable", "Lio/reactivex/disposables/CompositeDisposable;", "buildUseCaseCompletable", "Lio/reactivex/Completable;", "param", "(Ljava/lang/Object;)Lio/reactivex/Completable;", "dispose", "", "execute", "onSuccess", "Lio/reactivex/functions/Action;", "onError", "Lio/reactivex/functions/Consumer;", "", "(Lio/reactivex/functions/Action;Lio/reactivex/functions/Consumer;Ljava/lang/Object;)V", "app_devDebug"})
public abstract class SequentialCompletableUseCase<PARAM extends java.lang.Object> {
    private final io.reactivex.disposables.CompositeDisposable compositeDisposable = null;
    private final com.kyberswap.android.domain.SchedulerProvider schedulerProvider = null;
    
    public final void execute(@org.jetbrains.annotations.NotNull()
    io.reactivex.functions.Action onSuccess, @org.jetbrains.annotations.NotNull()
    io.reactivex.functions.Consumer<java.lang.Throwable> onError, PARAM param) {
    }
    
    @org.jetbrains.annotations.NotNull()
    protected abstract io.reactivex.Completable buildUseCaseCompletable(PARAM param);
    
    public final void dispose() {
    }
    
    protected SequentialCompletableUseCase(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.domain.SchedulerProvider schedulerProvider) {
        super();
    }
}