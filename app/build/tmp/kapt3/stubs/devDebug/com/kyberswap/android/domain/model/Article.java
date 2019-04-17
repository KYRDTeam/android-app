package com.kyberswap.android.domain.model;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u001c\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u000b\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0086\b\u0018\u0000 =2\u00020\u0001:\u0001=B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004B\u000f\b\u0016\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007B]\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\u0010\b\u0002\u0010\u000e\u001a\n\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000f\u0012\b\b\u0002\u0010\u0011\u001a\u00020\t\u0012\b\b\u0002\u0010\u0012\u001a\u00020\t\u0012\b\b\u0002\u0010\u0013\u001a\u00020\t\u0012\b\b\u0002\u0010\u0014\u001a\u00020\t\u00a2\u0006\u0002\u0010\u0015J\t\u0010\"\u001a\u00020\tH\u00c6\u0003J\t\u0010#\u001a\u00020\u000bH\u00c6\u0003J\t\u0010$\u001a\u00020\rH\u00c6\u0003J\u0011\u0010%\u001a\n\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000fH\u00c6\u0003J\t\u0010&\u001a\u00020\tH\u00c6\u0003J\t\u0010\'\u001a\u00020\tH\u00c6\u0003J\t\u0010(\u001a\u00020\tH\u00c6\u0003J\t\u0010)\u001a\u00020\tH\u00c6\u0003Ja\u0010*\u001a\u00020\u00002\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\r2\u0010\b\u0002\u0010\u000e\u001a\n\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000f2\b\b\u0002\u0010\u0011\u001a\u00020\t2\b\b\u0002\u0010\u0012\u001a\u00020\t2\b\b\u0002\u0010\u0013\u001a\u00020\t2\b\b\u0002\u0010\u0014\u001a\u00020\tH\u00c6\u0001J\b\u0010+\u001a\u00020\rH\u0016J\u0013\u0010,\u001a\u00020-2\b\u0010.\u001a\u0004\u0018\u00010/H\u00d6\u0003J\b\u00100\u001a\u00020\tH\u0016J\b\u00101\u001a\u00020\tH\u0016J\b\u00102\u001a\u00020\tH\u0016J\b\u00103\u001a\u00020\tH\u0016J\b\u00104\u001a\u00020\rH\u0016J\b\u00105\u001a\u00020\tH\u0016J\t\u00106\u001a\u00020\rH\u00d6\u0001J\u0006\u00107\u001a\u00020\tJ\u0006\u00108\u001a\u00020\tJ\t\u00109\u001a\u00020\tH\u00d6\u0001J\u0018\u0010:\u001a\u00020;2\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010<\u001a\u00020\rH\u0016R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0019\u0010\u000e\u001a\n\u0012\u0004\u0012\u00020\u0010\u0018\u00010\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0011\u0010\u0011\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0017R\u0011\u0010\u0012\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0017R\u0011\u0010\u0013\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u0017R\u0011\u0010\u0014\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0017\u00a8\u0006>"}, d2 = {"Lcom/kyberswap/android/domain/model/Article;", "Lcom/kyberswap/android/domain/model/QuoteArticle;", "parcel", "Landroid/os/Parcel;", "(Landroid/os/Parcel;)V", "entity", "Lcom/kyberswap/android/data/api/home/entity/ArticleEntity;", "(Lcom/kyberswap/android/data/api/home/entity/ArticleEntity;)V", "answerCount", "", "contributor", "Lcom/kyberswap/android/domain/model/Contributor;", "likeCount", "", "metaTags", "", "Lcom/kyberswap/android/domain/model/MetaTag;", "publishedAt", "thumbnailImageUrl", "title", "url", "(Ljava/lang/String;Lcom/kyberswap/android/domain/model/Contributor;ILjava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getAnswerCount", "()Ljava/lang/String;", "getContributor", "()Lcom/kyberswap/android/domain/model/Contributor;", "getLikeCount", "()I", "getMetaTags", "()Ljava/util/List;", "getPublishedAt", "getThumbnailImageUrl", "getTitle", "getUrl", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "describeContents", "equals", "", "other", "", "getArticleIconUrlQuote", "getAuthorIconUrlQuote", "getAuthorNameQuote", "getIdQuote", "getLikeCountQuote", "getTitleQuote", "hashCode", "preSlashAffiliation", "preSlashJobTitle", "toString", "writeToParcel", "", "flags", "CREATOR", "app_devDebug"})
public final class Article implements com.kyberswap.android.domain.model.QuoteArticle {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String answerCount = null;
    @org.jetbrains.annotations.NotNull()
    private final com.kyberswap.android.domain.model.Contributor contributor = null;
    private final int likeCount = 0;
    @org.jetbrains.annotations.Nullable()
    private final java.util.List<com.kyberswap.android.domain.model.MetaTag> metaTags = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String publishedAt = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String thumbnailImageUrl = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String title = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String url = null;
    public static final com.kyberswap.android.domain.model.Article.CREATOR CREATOR = null;
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String preSlashJobTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String preSlashAffiliation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String getArticleIconUrlQuote() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String getAuthorIconUrlQuote() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String getAuthorNameQuote() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String getIdQuote() {
        return null;
    }
    
    @java.lang.Override()
    public int getLikeCountQuote() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String getTitleQuote() {
        return null;
    }
    
    @java.lang.Override()
    public void writeToParcel(@org.jetbrains.annotations.NotNull()
    android.os.Parcel parcel, int flags) {
    }
    
    @java.lang.Override()
    public int describeContents() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAnswerCount() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.domain.model.Contributor getContributor() {
        return null;
    }
    
    public final int getLikeCount() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<com.kyberswap.android.domain.model.MetaTag> getMetaTags() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getPublishedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getThumbnailImageUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTitle() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUrl() {
        return null;
    }
    
    public Article(@org.jetbrains.annotations.NotNull()
    java.lang.String answerCount, @org.jetbrains.annotations.NotNull()
    com.kyberswap.android.domain.model.Contributor contributor, int likeCount, @org.jetbrains.annotations.Nullable()
    java.util.List<com.kyberswap.android.domain.model.MetaTag> metaTags, @org.jetbrains.annotations.NotNull()
    java.lang.String publishedAt, @org.jetbrains.annotations.NotNull()
    java.lang.String thumbnailImageUrl, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String url) {
        super();
    }
    
    public Article() {
        super();
    }
    
    public Article(@org.jetbrains.annotations.NotNull()
    android.os.Parcel parcel) {
        super();
    }
    
    public Article(@org.jetbrains.annotations.NotNull()
    com.kyberswap.android.data.api.home.entity.ArticleEntity entity) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.domain.model.Contributor component2() {
        return null;
    }
    
    public final int component3() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<com.kyberswap.android.domain.model.MetaTag> component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.kyberswap.android.domain.model.Article copy(@org.jetbrains.annotations.NotNull()
    java.lang.String answerCount, @org.jetbrains.annotations.NotNull()
    com.kyberswap.android.domain.model.Contributor contributor, int likeCount, @org.jetbrains.annotations.Nullable()
    java.util.List<com.kyberswap.android.domain.model.MetaTag> metaTags, @org.jetbrains.annotations.NotNull()
    java.lang.String publishedAt, @org.jetbrains.annotations.NotNull()
    java.lang.String thumbnailImageUrl, @org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String url) {
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
    
    @kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00022\u0006\u0010\u0005\u001a\u00020\u0006H\u0016J\u001d\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00020\b2\u0006\u0010\t\u001a\u00020\nH\u0016\u00a2\u0006\u0002\u0010\u000b\u00a8\u0006\f"}, d2 = {"Lcom/kyberswap/android/domain/model/Article$CREATOR;", "Landroid/os/Parcelable$Creator;", "Lcom/kyberswap/android/domain/model/Article;", "()V", "createFromParcel", "parcel", "Landroid/os/Parcel;", "newArray", "", "size", "", "(I)[Lcom/kyberswap/android/domain/model/Article;", "app_devDebug"})
    public static final class CREATOR implements android.os.Parcelable.Creator<com.kyberswap.android.domain.model.Article> {
        
        @org.jetbrains.annotations.NotNull()
        @java.lang.Override()
        public com.kyberswap.android.domain.model.Article createFromParcel(@org.jetbrains.annotations.NotNull()
        android.os.Parcel parcel) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        @java.lang.Override()
        public com.kyberswap.android.domain.model.Article[] newArray(int size) {
            return null;
        }
        
        private CREATOR() {
            super();
        }
    }
}