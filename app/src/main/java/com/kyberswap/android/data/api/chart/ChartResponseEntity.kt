package com.kyberswap.android.data.api.chart

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class ChartResponseEntity(
    @SerializedName("c")
    val c: List<BigDecimal>? = listOf(),
    @SerializedName("h")
    val h: List<BigDecimal>? = listOf(),
    @SerializedName("l")
    val l: List<BigDecimal>? = listOf(),
    @SerializedName("o")
    val o: List<BigDecimal>? = listOf(),
    @SerializedName("s")
    val s: String? = "",
    @SerializedName("t")
    val t: List<BigDecimal>? = listOf()
)