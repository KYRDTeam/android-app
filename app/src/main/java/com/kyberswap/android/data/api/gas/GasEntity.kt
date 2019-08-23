package com.kyberswap.android.data.api.gas

import com.google.gson.annotations.SerializedName

data class GasEntity(
    @SerializedName("default")
    val default: String = "",
    @SerializedName("fast")
    val fast: String = "",
    @SerializedName("low")
    val low: String = "",
    @SerializedName("standard")
    val standard: String = ""
)