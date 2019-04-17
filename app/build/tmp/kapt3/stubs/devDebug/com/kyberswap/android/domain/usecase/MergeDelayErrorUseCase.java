package com.kyberswap.android.domain.usecase;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0002\b&\u0018\u0000*\u0006\b\u0000\u0010\u0001 \u0000*\u0004\b\u0001\u0010\u00022\u00020\u0003B\u000f\b\u0004\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u001b\u0010\t\u001a\b\u0012\u0004\u0012\u00028\u00010\n2\u0006\u0010\u000b\u001a\u00028\u0000H$\u00a2\u0006\u0002\u0010\fJ\u0006\u0010\r\u001a\u00020\u000eJ/\u0010\u000f\u001a\u00020\u000e2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00028\u00010\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u00112\u0006\u0010\u000b\u001a\u00028\u0000\u00a2\u0006\u0002\u0010\u0014R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcom/kyberswap/android/domain/usecase/MergeDelayErrorUseCase;", "PARAM", "RESPONSE", "", "schedulerProvider", "Lcom/kyberswap/android/domain/SchedulerProvider;", "(Lcom/kyberswap/android/domain/SchedulerProvider;)V", "compositeDisposable", "Lio/reactivex/disposables/CompositeDisposable;", "buildUseCaseFlowable", "Lio/reactivex/Flowable;", "param", "(Ljava/lang/Object;)Lio/reactivex/Flowable;", "dispose", "", "execute", "onSuccess", "Lio/reactivex/functions/Consumer;", "onError", "", "(Lio/reactivex/functions/Consumer;Lio/reactivex/functions/Consumer;Ljava/lang/Object;)V", "app_devDebug"})
public abstract class MergeDelayErrorUseCase<PARAM extends java.lang.Object, RESPONSE extends java.lang.Object> {
    private final io.reactivex.disposables.CompositeDisposable compositeDisposable = null;
    private final com.kyberswap.android.domain.SchedulerProvider schedulerProvider = null;
    
    public final void execute(@org.jetbrains.annotations.NotNull()
    io.reactivex.functions.Consumer<RESPONSE> onSuccess, @org.jetbrains.annotations.NotNull()
    io.reactivex.functions.Consumer<java.lang.Throwable> onError, PARAM param) {
    }
    
    @org.jetbrains.annotations.NotNull()
    protected abstract io.reactivex.Flowable<RESPONSE> buildUseCaseFlowable(PARAM param);
    
    public final void dispose() {
    }
    
    protected MergeDelayErrorUseCase(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.domain.SchedulerProvider schedulerProvider) {
        super();
    }
}