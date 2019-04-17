package com.kyberswap.android.data.api.home;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\"\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020\bH\'J\u0018\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u00032\b\b\u0001\u0010\u000b\u001a\u00020\fH\'J\u0018\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0007\u001a\u00020\bH\'\u00a8\u0006\u000e"}, d2 = {"Lcom/kyberswap/android/data/api/home/HomeApi;", "", "getFeatureArticles", "Lio/reactivex/Single;", "Lcom/kyberswap/android/data/api/home/entity/ArticlesEntity;", "featureArticleId", "", "page", "", "getHeaders", "Lcom/kyberswap/android/data/api/home/entity/HeaderEntity;", "type", "", "getTopArticles", "app_devDebug"})
public abstract interface HomeApi {
    
    @org.jetbrains.annotations.NotNull()
    @retrofit2.http.GET(value = "headers")
    public abstract io.reactivex.Single<com.kyberswap.android.data.api.home.entity.HeaderEntity> getHeaders(@org.jetbrains.annotations.NotNull()
    @retrofit2.http.Query(value = "type")
    java.lang.String type);
    
    @org.jetbrains.annotations.NotNull()
    @retrofit2.http.GET(value = "articles/top/hairsalon/{page}")
    public abstract io.reactivex.Single<com.kyberswap.android.data.api.home.entity.ArticlesEntity> getTopArticles(@retrofit2.http.Path(value = "page")
    int page);
    
    @org.jetbrains.annotations.NotNull()
    @retrofit2.http.GET(value = "articles/features/hairsalon/{featureArticleId}/{page}")
    public abstract io.reactivex.Single<com.kyberswap.android.data.api.home.entity.ArticlesEntity> getFeatureArticles(@retrofit2.http.Path(value = "featureArticleId")
    long featureArticleId, @retrofit2.http.Path(value = "page")
    int page);
}