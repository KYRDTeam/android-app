package com.kyberswap.android.data.api.home.entity;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0012\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B3\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\b\u0012\u0006\u0010\n\u001a\u00020\b\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\bH\u00c6\u0003J\t\u0010\u0017\u001a\u00020\bH\u00c6\u0003J\t\u0010\u0018\u001a\u00020\bH\u00c6\u0003JA\u0010\u0019\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\bH\u00c6\u0001J\u0013\u0010\u001a\u001a\u00020\u001b2\b\u0010\u001c\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001d\u001a\u00020\bH\u00d6\u0001J\t\u0010\u001e\u001a\u00020\u001fH\u00d6\u0001R\u001c\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00058\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0016\u0010\u0007\u001a\u00020\b8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0016\u0010\t\u001a\u00020\b8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0011R\u0016\u0010\n\u001a\u00020\b8\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011\u00a8\u0006 "}, d2 = {"Lcom/kyberswap/android/data/api/home/entity/ArticlesEntity;", "", "links", "Lcom/kyberswap/android/data/api/common/entity/LinksEntity;", "article", "", "Lcom/kyberswap/android/data/api/home/entity/ArticleEntity;", "page", "", "size", "totalHits", "(Lcom/kyberswap/android/data/api/common/entity/LinksEntity;Ljava/util/List;III)V", "getArticle", "()Ljava/util/List;", "getLinks", "()Lcom/kyberswap/android/data/api/common/entity/LinksEntity;", "getPage", "()I", "getSize", "getTotalHits", "component1", "component2", "component3", "component4", "component5", "copy", "equals", "", "other", "hashCode", "toString", "", "app_devDebug"})
public final class ArticlesEntity {
    @org.jetbrains.annotations.NotNull()
    @com.google.gson.annotations.SerializedName(value = "_links")
    private final com.kyberswap.android.data.api.common.entity.LinksEntity links = null;
    @org.jetbrains.annotations.NotNull()
    @com.google.gson.annotations.SerializedName(value = "contents")
    private final java.util.List<com.kyberswap.android.data.api.home.entity.ArticleEntity> article = null;
    @com.google.gson.annotations.SerializedName(value = "page")
    private final int page = 0;
    @com.google.gson.annotations.SerializedName(value = "size")
    private final int size = 0;
    @com.google.gson.annotations.SerializedName(value = "total_hits")
    private final int totalHits = 0;
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.data.api.common.entity.LinksEntity getLinks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.kyberswap.android.data.api.home.entity.ArticleEntity> getArticle() {
        return null;
    }
    
    public final int getPage() {
        return 0;
    }
    
    public final int getSize() {
        return 0;
    }
    
    public final int getTotalHits() {
        return 0;
    }
    
    public ArticlesEntity(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.common.entity.LinksEntity links, @org.jetbrains.annotations.NotNull()
    java.util.List<com.kyberswap.android.data.api.home.entity.ArticleEntity> article, int page, int size, int totalHits) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.data.api.common.entity.LinksEntity component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.kyberswap.android.data.api.home.entity.ArticleEntity> component2() {
        return null;
    }
    
    public final int component3() {
        return 0;
    }
    
    public final int component4() {
        return 0;
    }
    
    public final int component5() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.data.api.home.entity.ArticlesEntity copy(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.common.entity.LinksEntity links, @org.jetbrains.annotations.NotNull()
    java.util.List<com.kyberswap.android.data.api.home.entity.ArticleEntity> article, int page, int size, int totalHits) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object p0) {
        return false;
    }
}