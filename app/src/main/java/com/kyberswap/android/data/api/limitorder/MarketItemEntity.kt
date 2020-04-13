package com.kyberswap.android.data.api.limitorder

import com.google.gson.annotations.SerializedName

data class MarketItemEntity(
    @SerializedName("buy_price")
    val buyPrice: String = "",
    @SerializedName("change")
    val change: String = "",
    @SerializedName("pair")
    val pair: String = "",
    @SerializedName("sell_price")
    val sellPrice: String = "",
    @SerializedName("volume")
    val volume: String = ""
)