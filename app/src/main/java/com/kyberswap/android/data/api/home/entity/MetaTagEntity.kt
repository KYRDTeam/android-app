package com.kyberswap.android.data.api.home.entity

import com.google.gson.annotations.SerializedName

data class MetaTagEntity(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("tagged_at") val taggedAt: String
)