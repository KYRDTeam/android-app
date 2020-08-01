package com.kyberswap.android.data.api.campaign


import com.google.gson.annotations.SerializedName

data class CampaignEntity(
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("link")
    val link: String?
)