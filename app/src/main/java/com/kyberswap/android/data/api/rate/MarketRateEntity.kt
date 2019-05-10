package com.kyberswap.android.data.api.rate

import com.google.gson.annotations.SerializedName

data class MarketRateEntity(
    @SerializedName("data")
    val `data`: List<RateEntity> = listOf(),
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("updateAt")
    val updateAt: Int = 0
)