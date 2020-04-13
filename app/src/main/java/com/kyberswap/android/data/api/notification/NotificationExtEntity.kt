package com.kyberswap.android.data.api.notification


import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class NotificationExtEntity(
    @SerializedName("type")
    val type: String? = "",
    @SerializedName("order_id")
    val orderId: Long? = 0,
    @SerializedName("src_token")
    val srcToken: String? = "",
    @SerializedName("dst_token")
    val dstToken: String? = "",
    @SerializedName("min_rate")
    val minRate: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("src_amount")
    val srcAmount: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("fee")
    val fee: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("transfer_fee")
    val transferFee: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("receive")
    val receive: BigDecimal? = BigDecimal.ZERO,
    @SerializedName("sender")
    val sender: String? = "",
    @SerializedName("created_at")
    val createdAt: Long? = 0,
    @SerializedName("updated_at")
    val updatedAt: Long? = 0,
    @SerializedName("tx_hash")
    val txHash: String? = "",
    @SerializedName("alert_id")
    val alertId: Long? = 0L,
    @SerializedName("base")
    val base: String? = "",
    @SerializedName("token")
    val token: String? = "",
    val percent: String? = "",
    @SerializedName("side_trade")
    val sideTrade: String? = ""
)