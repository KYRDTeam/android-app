package com.kyberswap.android.data.api.chart


import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class Data(
    @SerializedName("timestamp")
    val timestamp: Long? = 0,
    @SerializedName("quote_symbol")
    val quoteSymbol: String? = "",
    @SerializedName("quote_name")
    val quoteName: String? = "",
    @SerializedName("quote_decimals")
    val quoteDecimals: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("quote_address")
    val quoteAddress: String? = "",
    @SerializedName("base_symbol")
    val baseSymbol: String? = "",
    @SerializedName("base_name")
    val baseName: String? = "",
    @SerializedName("base_decimals")
    val baseDecimals: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("base_address")
    val baseAddress: String? = "",
    @SerializedName("past_24h_high")
    val past24hHigh: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("past_24h_low")
    val past24hLow: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("usd_24h_volume")
    val usd24hVolume: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("eth_24h_volume")
    val eth24hVolume: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("token_24h_volume")
    val token24hVolume: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("current_bid")
    val currentBid: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("current_ask")
    val currentAsk: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("last_traded")
    val lastTraded: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("pair")
    val pair: String? = "",
    @SerializedName("custom_proxy")
    val customProxy: Boolean? = false,
    @SerializedName("original_token")
    val originalToken: String? = ""
)