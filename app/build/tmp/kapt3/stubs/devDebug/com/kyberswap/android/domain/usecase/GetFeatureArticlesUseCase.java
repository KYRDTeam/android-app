package com.kyberswap.android.domain.usecase;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u0014\u0012\u0004\u0012\u00020\u0002\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\u00030\u0001:\u0001\rB\u0017\b\u0007\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u00a2\u0006\u0002\u0010\tJ\u001c\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\u00030\u000b2\u0006\u0010\f\u001a\u00020\u0002H\u0016R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcom/kyberswap/android/domain/usecase/GetFeatureArticlesUseCase;", "Lcom/kyberswap/android/domain/usecase/SequentialUseCase;", "Lcom/kyberswap/android/domain/usecase/GetFeatureArticlesUseCase$Param;", "", "Lcom/kyberswap/android/domain/model/Article;", "schedulerProvider", "Lcom/kyberswap/android/domain/SchedulerProvider;", "featureArticleRepository", "Lcom/kyberswap/android/domain/repository/FeatureArticleRepository;", "(Lcom/kyberswap/android/domain/SchedulerProvider;Lcom/kyberswap/android/domain/repository/FeatureArticleRepository;)V", "buildUseCaseSingle", "Lio/reactivex/Single;", "param", "Param", "app_devDebug"})
public final class GetFeatureArticlesUseCase extends com.kyberswap.android.domain.usecase.SequentialUseCase<com.kyberswap.android.domain.usecase.GetFeatureArticlesUseCase.Param, java.util.List<? extends com.kyberswap.android.domain.model.Article>> {
    private final com.kyberswap.android.domain.repository.FeatureArticleRepository featureArticleRepository = null;
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public io.reactivex.Single<java.util.List<com.kyberswap.android.domain.model.Article>> buildUseCaseSingle(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.domain.usecase.GetFeatureArticlesUseCase.Param param) {
        return null;
    }
    
    @javax.inject.Inject()
    public GetFeatureArticlesUseCase(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.domain.SchedulerProvider schedulerProvider, @org.jetbrains.annotations.NotNull()
    com.kyberswap.android.domain.repository.FeatureArticleRepository featureArticleRepository) {
        super(null);
    }
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\n\u00a8\u0006\u000b"}, d2 = {"Lcom/kyberswap/android/domain/usecase/GetFeatureArticlesUseCase$Param;", "", "featureArticleId", "", "pageNumber", "", "(JI)V", "getFeatureArticleId", "()J", "getPageNumber", "()I", "app_devDebug"})
    public static final class Param {
        private final long featureArticleId = 0L;
        private final int pageNumber = 0;
        
        public final long getFeatureArticleId() {
            return 0L;
        }
        
        public final int getPageNumber() {
            return 0;
        }
        
        public Param(long featureArticleId, int pageNumber) {
            super();
        }
    }
}