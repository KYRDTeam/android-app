package com.kyberswap.android.data.repository;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0000\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J$\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/kyberswap/android/data/repository/FeatureArticleDataRepository;", "Lcom/kyberswap/android/domain/repository/FeatureArticleRepository;", "homeApi", "Lcom/kyberswap/android/data/api/home/HomeApi;", "mapper", "Lcom/kyberswap/android/data/mapper/ArticleMapper;", "(Lcom/kyberswap/android/data/api/home/HomeApi;Lcom/kyberswap/android/data/mapper/ArticleMapper;)V", "featureArticles", "Lio/reactivex/Single;", "", "Lcom/kyberswap/android/domain/model/Article;", "articleFeatureId", "", "page", "", "app_devDebug"})
public final class FeatureArticleDataRepository implements com.kyberswap.android.domain.repository.FeatureArticleRepository {
    private final com.kyberswap.android.data.api.home.HomeApi homeApi = null;
    private final com.kyberswap.android.data.mapper.ArticleMapper mapper = null;
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public io.reactivex.Single<java.util.List<com.kyberswap.android.domain.model.Article>> featureArticles(long articleFeatureId, int page) {
        return null;
    }
    
    @javax.inject.Inject()
    public FeatureArticleDataRepository(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.HomeApi homeApi, @org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.mapper.ArticleMapper mapper) {
        super();
    }
}