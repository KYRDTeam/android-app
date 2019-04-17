package com.kyberswap.android.domain.model

import android.os.Parcelable

interface QuoteArticle : Parcelable {
    fun getArticleIconUrlQuote(): String
    fun getAuthorIconUrlQuote(): String
    fun getAuthorNameQuote(): String
    fun getIdQuote(): String
    fun getLikeCountQuote(): Int
    fun getTitleQuote(): String
}
