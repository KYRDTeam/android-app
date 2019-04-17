package com.kyberswap.android.data.api.home.entity;

import java.lang.System;

@android.arch.persistence.room.Entity(tableName = "HeaderEntity")
@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u001e\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001BG\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0003\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\b\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\b\u00a2\u0006\u0002\u0010\rJ\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\t\u0010$\u001a\u00020\bH\u00c6\u0003J\t\u0010%\u001a\u00020\bH\u00c6\u0003J\t\u0010&\u001a\u00020\u000bH\u00c6\u0003J\t\u0010\'\u001a\u00020\bH\u00c6\u0003JK\u0010(\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0003\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\bH\u00c6\u0001J\u0013\u0010)\u001a\u00020*2\b\u0010+\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010,\u001a\u00020-H\u00d6\u0001J\t\u0010.\u001a\u00020\u000bH\u00d6\u0001R$\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00058\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u001e\u0010\t\u001a\u00020\b8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R\u001e\u0010\u0002\u001a\u00020\u00038\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019R\u001e\u0010\u0007\u001a\u00020\b8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001a\u0010\u0013\"\u0004\b\u001b\u0010\u0015R\u001e\u0010\n\u001a\u00020\u000b8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001c\u0010\u001d\"\u0004\b\u001e\u0010\u001fR\u001e\u0010\f\u001a\u00020\b8\u0006@\u0006X\u0087\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b \u0010\u0013\"\u0004\b!\u0010\u0015\u00a8\u0006/"}, d2 = {"Lcom/kyberswap/android/data/api/home/entity/HeaderEntity;", "", "links", "Lcom/kyberswap/android/data/api/common/entity/LinksEntity;", "articleFeatures", "", "Lcom/kyberswap/android/data/api/home/entity/ArticleFeatureEntity;", "reviewCount", "", "likeCount", "type", "", "uid", "(Lcom/kyberswap/android/data/api/common/entity/LinksEntity;Ljava/util/List;JJLjava/lang/String;J)V", "getArticleFeatures", "()Ljava/util/List;", "setArticleFeatures", "(Ljava/util/List;)V", "getLikeCount", "()J", "setLikeCount", "(J)V", "getLinks", "()Lcom/kyberswap/android/data/api/common/entity/LinksEntity;", "setLinks", "(Lcom/kyberswap/android/data/api/common/entity/LinksEntity;)V", "getReviewCount", "setReviewCount", "getType", "()Ljava/lang/String;", "setType", "(Ljava/lang/String;)V", "getUid", "setUid", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "", "other", "hashCode", "", "toString", "app_devDebug"})
public final class HeaderEntity {
    @org.jetbrains.annotations.NotNull()
    @android.arch.persistence.room.Ignore()
    @com.google.gson.annotations.SerializedName(value = "_links")
    private com.kyberswap.android.data.api.common.entity.LinksEntity links;
    @org.jetbrains.annotations.NotNull()
    @com.google.gson.annotations.SerializedName(value = "article_features")
    private java.util.List<com.kyberswap.android.data.api.home.entity.ArticleFeatureEntity> articleFeatures;
    @com.google.gson.annotations.SerializedName(value = "review_count")
    private long reviewCount;
    @com.google.gson.annotations.SerializedName(value = "like_count")
    private long likeCount;
    @org.jetbrains.annotations.NotNull()
    @com.google.gson.annotations.SerializedName(value = "type")
    private java.lang.String type;
    @android.arch.persistence.room.PrimaryKey(autoGenerate = true)
    private long uid;
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.data.api.common.entity.LinksEntity getLinks() {
        return null;
    }
    
    public final void setLinks(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.common.entity.LinksEntity p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.kyberswap.android.data.api.home.entity.ArticleFeatureEntity> getArticleFeatures() {
        return null;
    }
    
    public final void setArticleFeatures(@org.jetbrains.annotations.NotNull()
    java.util.List<com.kyberswap.android.data.api.home.entity.ArticleFeatureEntity> p0) {
    }
    
    public final long getReviewCount() {
        return 0L;
    }
    
    public final void setReviewCount(long p0) {
    }
    
    public final long getLikeCount() {
        return 0L;
    }
    
    public final void setLikeCount(long p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getType() {
        return null;
    }
    
    public final void setType(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final long getUid() {
        return 0L;
    }
    
    public final void setUid(long p0) {
    }
    
    public HeaderEntity(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.common.entity.LinksEntity links, @org.jetbrains.annotations.NotNull()
    @android.arch.persistence.room.TypeConverters(value = {com.kyberswap.android.data.repository.datasource.local.HeaderTypeConverter.class})
    java.util.List<com.kyberswap.android.data.api.home.entity.ArticleFeatureEntity> articleFeatures, long reviewCount, long likeCount, @org.jetbrains.annotations.NotNull()
    java.lang.String type, long uid) {
        super();
    }
    
    public HeaderEntity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.data.api.common.entity.LinksEntity component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.kyberswap.android.data.api.home.entity.ArticleFeatureEntity> component2() {
        return null;
    }
    
    public final long component3() {
        return 0L;
    }
    
    public final long component4() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    public final long component6() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.data.api.home.entity.HeaderEntity copy(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.common.entity.LinksEntity links, @org.jetbrains.annotations.NotNull()
    @android.arch.persistence.room.TypeConverters(value = {com.kyberswap.android.data.repository.datasource.local.HeaderTypeConverter.class})
    java.util.List<com.kyberswap.android.data.api.home.entity.ArticleFeatureEntity> articleFeatures, long reviewCount, long likeCount, @org.jetbrains.annotations.NotNull()
    java.lang.String type, long uid) {
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