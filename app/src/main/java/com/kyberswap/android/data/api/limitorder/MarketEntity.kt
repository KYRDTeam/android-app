package com.kyberswap.android.data.api.limitorder

import com.google.gson.annotations.SerializedName

data class MarketEntity(
    @SerializedName("data")
    val `data`: List<MarketItemEntity> = listOf(),
    @SerializedName("error")
    val error: Boolean = false,
    @SerializedName("timestamp")
    val timestamp: Int = 0
)