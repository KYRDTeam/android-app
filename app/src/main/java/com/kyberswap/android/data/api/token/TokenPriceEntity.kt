package com.kyberswap.android.data.api.token

import com.google.gson.annotations.SerializedName

data class TokenPriceEntity(
    @SerializedName("data")
    val `data`: List<Data> = listOf(),
    @SerializedName("error")
    val error: Boolean = false,
    @SerializedName("timestamp")
    val timestamp: Int = 0
)