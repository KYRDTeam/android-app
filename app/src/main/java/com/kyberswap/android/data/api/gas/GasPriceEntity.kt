package com.kyberswap.android.data.api.gas

import com.google.gson.annotations.SerializedName

data class GasPriceEntity(
    @SerializedName("data")
    val `data`: GasEntity = GasEntity(),
    @SerializedName("success")
    val success: Boolean = false
)