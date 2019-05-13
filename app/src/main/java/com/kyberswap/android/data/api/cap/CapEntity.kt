package com.kyberswap.android.data.api.cap

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CapEntity(
    @SerializedName("cap")
    val cap: BigDecimal = BigDecimal.ZERO,
    @SerializedName("kyced")
    val kyced: Boolean = false,
    @SerializedName("rich")
    val rich: Boolean = false
)