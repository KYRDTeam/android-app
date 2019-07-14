package com.kyberswap.android.domain.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Parcelize
data class NotificationLimitOrder(
    @SerializedName("type")
    val type: String = "",
    @SerializedName("order_id")
    val orderId: Long = 0,
    @SerializedName("src_token")
    val srcToken: String = "",
    @SerializedName("dst_token")
    val dstToken: String = "",
    @SerializedName("min_rate")
    val minRate: BigDecimal = BigDecimal.ZERO,
    @SerializedName("src_amount")
    val srcAmount: BigDecimal = BigDecimal.ZERO,
    @SerializedName("fee")
    val fee: BigDecimal = BigDecimal.ZERO,
    @SerializedName("sender")
    val sender: String = "",
    @SerializedName("created_at")
    val createdAt: Long = 0,
    @SerializedName("updated_at")
    val updatedAt: Long = 0,
    @SerializedName("tx_hash")
    val txHash: String = "",
    @SerializedName("test_order_id")
    val testOrderId: String = ""
) : Parcelable