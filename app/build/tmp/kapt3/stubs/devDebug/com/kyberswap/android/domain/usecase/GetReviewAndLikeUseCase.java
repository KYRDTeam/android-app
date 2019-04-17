package com.kyberswap.android.domain.usecase;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u0010\u0012\u0006\u0012\u0004\u0018\u00010\u0002\u0012\u0004\u0012\u00020\u00030\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0018\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00030\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\u0002H\u0016R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/kyberswap/android/domain/usecase/GetReviewAndLikeUseCase;", "Lcom/kyberswap/android/domain/usecase/FlowableUseCase;", "", "Lcom/kyberswap/android/domain/model/HeaderInfo;", "schedulerProvider", "Lcom/kyberswap/android/domain/SchedulerProvider;", "headersRepository", "Lcom/kyberswap/android/domain/repository/HeaderRepository;", "(Lcom/kyberswap/android/domain/SchedulerProvider;Lcom/kyberswap/android/domain/repository/HeaderRepository;)V", "buildUseCaseFlowable", "Lio/reactivex/Flowable;", "param", "app_devDebug"})
public final class GetReviewAndLikeUseCase extends com.kyberswap.android.domain.usecase.FlowableUseCase<java.lang.String, com.kyberswap.android.domain.model.HeaderInfo> {
    private final com.kyberswap.android.domain.repository.HeaderRepository headersRepository = null;
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public io.reactivex.Flowable<com.kyberswap.android.domain.model.HeaderInfo> buildUseCaseFlowable(@org.jetbrains.annotations.Nullable()
    java.lang.String param) {
        return null;
    }
    
    @javax.inject.Inject()
    public GetReviewAndLikeUseCase(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.domain.SchedulerProvider schedulerProvider, @org.jetbrains.annotations.NotNull()
    com.kyberswap.android.domain.repository.HeaderRepository headersRepository) {
        super(null);
    }
}