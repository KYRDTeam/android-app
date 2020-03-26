package com.kyberswap.android.data.api.gas

import com.google.gson.annotations.SerializedName

data class MaxGasPriceEntity(
    @SerializedName("data")
    val `data`: String = "",
    @SerializedName("success")
    val success: Boolean = false
)