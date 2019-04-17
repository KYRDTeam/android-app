package com.kyberswap.android.data.api.home.entity

import com.google.gson.annotations.SerializedName

data class ArticleEntity(
    @SerializedName("answer_count") val answerCount: String = "",
    @SerializedName("contributor") val contributor: ContributorEntity = ContributorEntity(),
    @SerializedName("like_count") val likeCount: Int = 0,
    @SerializedName("meta_tag") val metaTags: List<MetaTagEntity?>? = listOf(),
    @SerializedName("published_at") val publishedAt: String = "",
    @SerializedName("thumbnail_image_url") val thumbnailImageUrl: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("url") val url: String = ""
)