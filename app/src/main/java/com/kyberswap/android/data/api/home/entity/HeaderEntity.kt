package com.kyberswap.android.data.api.home.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.kyberswap.android.data.api.common.entity.LinksEntity
import com.kyberswap.android.data.repository.datasource.local.HeaderTypeConverter

@Entity(tableName = "HeaderEntity")
data class HeaderEntity(
    @Ignore
    @SerializedName("_links") var links: LinksEntity = LinksEntity(),
    @TypeConverters(HeaderTypeConverter::class)
    @SerializedName("article_features") var articleFeatures: List<ArticleFeatureEntity> = listOf(),
    @SerializedName("review_count") var reviewCount: Long = 0,
    @SerializedName("like_count") var likeCount: Long = 0,
    @SerializedName("type") var type: String = "",
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0
)