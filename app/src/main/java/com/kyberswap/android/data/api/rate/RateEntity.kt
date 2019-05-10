package com.kyberswap.android.data.api.rate

import com.google.gson.annotations.SerializedName

data class RateEntity(
    @SerializedName("dest")
    val dest: String = "",
    @SerializedName("minRate")
    val minRate: String = "",
    @SerializedName("rate")
    val rate: String = "",
    @SerializedName("source")
    val source: String = ""
)