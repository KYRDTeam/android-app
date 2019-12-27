package com.kyberswap.android.data.api.chart


import com.google.gson.annotations.SerializedName

data class MarketEntity(
    @SerializedName("data")
    val `data`: List<Data> = listOf(),
    @SerializedName("error")
    val error: Boolean?
)