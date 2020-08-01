package com.kyberswap.android.data.api.campaign


import com.google.gson.annotations.SerializedName

data class CampaignResponseEntity(
    @SerializedName("data")
    val `data`: List<CampaignEntity>,
    @SerializedName("success")
    val success: Boolean?
)