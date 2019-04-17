package com.kyberswap.android.domain.model;

import java.lang.System;

@kotlin.Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 1, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\b\u0010\u0004\u001a\u00020\u0003H&J\b\u0010\u0005\u001a\u00020\u0003H&J\b\u0010\u0006\u001a\u00020\u0003H&J\b\u0010\u0007\u001a\u00020\bH&J\b\u0010\t\u001a\u00020\u0003H&\u00a8\u0006\n"}, d2 = {"Lcom/kyberswap/android/domain/model/QuoteArticle;", "Landroid/os/Parcelable;", "getArticleIconUrlQuote", "", "getAuthorIconUrlQuote", "getAuthorNameQuote", "getIdQuote", "getLikeCountQuote", "", "getTitleQuote", "app_devDebug"})
public abstract interface QuoteArticle extends android.os.Parcelable {
    
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String getArticleIconUrlQuote();
    
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String getAuthorIconUrlQuote();
    
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String getAuthorNameQuote();
    
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String getIdQuote();
    
    public abstract int getLikeCountQuote();
    
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String getTitleQuote();
}