package com.kyberswap.android.data.api.limitorder


import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class PendingBalancesEntity(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("data")
    val `data`: Map<String, BigDecimal> = HashMap()
)