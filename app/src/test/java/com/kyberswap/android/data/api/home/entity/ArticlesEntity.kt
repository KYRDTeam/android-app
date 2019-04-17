package com.kyberswap.android.data.api.home.entity

import com.google.gson.annotations.SerializedName
import com.kyberswap.android.data.api.common.entity.LinksEntity

data class ArticlesEntity(
    @SerializedName("_links") val links: LinksEntity,
    @SerializedName("contents") val article: List<ArticleEntity>,
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("total_hits") val totalHits: Int
)