package com.kyberswap.android.data.api.rate

import com.google.gson.annotations.SerializedName

data class ExpectedRateEntity(
    @SerializedName("error")
    val error: Boolean = false,
    @SerializedName("expectedRate")
    val expectedRate: String = "",
    @SerializedName("slippageRate")
    val slippageRate: String = "",
    @SerializedName("timestamp")
    val timestamp: Int = 0
)