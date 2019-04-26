package com.kyberswap.android.data.api.token

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class TokenEntity(
    @SerializedName("timestamp")
    val timestamp: Long = 0,
    @SerializedName("token_symbol")
    val tokenSymbol: String = "",
    @SerializedName("token_name")
    val tokenName: String = "",
    @SerializedName("token_address")
    val tokenAddress: String = "",
    @SerializedName("token_decimal")
    val tokenDecimal: Int = 0,
    @SerializedName("rate_eth_now")
    val rateEthNow: BigDecimal = BigDecimal.ZERO,
    @SerializedName("change_eth_24h")
    val changeEth24h: BigDecimal = BigDecimal.ZERO,
    @SerializedName("rate_usd_now")
    val rateUsdNow: BigDecimal = BigDecimal.ZERO,
    @SerializedName("change_usd_24h")
    val changeUsd24h: BigDecimal = BigDecimal.ZERO
)