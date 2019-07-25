package com.kyberswap.android.data.api.currencies

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class TokenCurrencyEntity(
    @SerializedName("address")
    val address: String = "",
    @SerializedName("cg_id")
    val cgId: String = "",
    @SerializedName("decimals")
    val decimals: Int = 0,
    @SerializedName("gasApprove")
    val gasApprove: BigDecimal = BigDecimal.ZERO,
    @SerializedName("gasLimit")
    val gasLimit: String = "",
    @SerializedName("listing_time")
    val listingTime: Long = 0,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("priority")
    val priority: Boolean = false,
    @SerializedName("symbol")
    val symbol: String = "",
    @SerializedName("sp_limit_order")
    val spLimitOrder: Boolean? = false,
    @SerializedName("is_quote")
    val isQuote: Boolean? = false

)