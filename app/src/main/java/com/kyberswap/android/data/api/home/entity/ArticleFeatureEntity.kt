package com.kyberswap.android.data.api.home.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "ArticleFeatureEntities")
data class ArticleFeatureEntity(
    @SerializedName("end_datetime") var endDatetime: String,
    @PrimaryKey @SerializedName("id") var id: Long,
    @SerializedName("label") var label: String,
    @SerializedName("order") var order: Long,
    @SerializedName("start_datetime") var startDatetime: String
)
