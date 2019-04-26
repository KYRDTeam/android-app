package com.kyberswap.android.data.api.token

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Data(
    @SerializedName("price")
    val price: BigDecimal = BigDecimal.ZERO,
    @SerializedName("symbol")
    val symbol: String = ""
)