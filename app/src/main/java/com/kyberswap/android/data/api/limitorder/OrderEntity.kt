package com.kyberswap.android.data.api.limitorder


import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class OrderEntity(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("user_addr")
    val userAddr: String = "",
    @SerializedName("src")
    val src: String = "",
    @SerializedName("dst")
    val dst: String = "",
    @SerializedName("src_amount")
    val srcAmount: BigDecimal = BigDecimal.ZERO,
    @SerializedName("min_rate")
    val minRate: BigDecimal = BigDecimal.ZERO,
    @SerializedName("dest_addr")
    val destAddr: String = "",
    @SerializedName("nonce")
    val nonce: String = "",
    @SerializedName("fee")
    val fee: BigDecimal = BigDecimal.ZERO,
    @SerializedName("status")
    val status: String = "",
    @SerializedName("tx_hash")
    val txHash: String = "",
    @SerializedName("created_at")
    val createdAt: Long = 0,
    @SerializedName("updated_at")
    val updatedAt: Long = 0

)

fun JsonArray.toOrderEntity(jsonArray: JsonArray): OrderEntity {
    val keys = jsonArray.toList().map {
        it.asString
    }

    val id = get(keys.indexOf("id"))?.asInt ?: 0
    val userAddr = get(keys.indexOf("addr"))?.asString ?: ""
    val src = get(keys.indexOf("src"))?.asString ?: ""
    val dst = get(keys.indexOf("dst"))?.asString ?: ""
    val srcAmount =
        get(keys.indexOf("src_amount"))?.asBigDecimal ?: BigDecimal.ZERO
    val minRate = get(keys.indexOf("min_rate"))?.asBigDecimal ?: BigDecimal.ZERO
    val destAddr = get(keys.indexOf("dest_addr"))?.asString ?: ""
    val nonce = get(keys.indexOf("nonce"))?.asString ?: ""
    val fee = get(keys.indexOf("fee"))?.asBigDecimal ?: BigDecimal.ZERO
    val status = get(keys.indexOf("status"))?.asString ?: ""
    val txHash = get(keys.indexOf("tx_hash"))?.asString ?: ""
    val createdAt = get(keys.indexOf("created_at"))?.asLong ?: 0
    val updatedAt = get(keys.indexOf("updated_at"))?.asLong ?: 0

    return OrderEntity(
        id,
        userAddr,
        src,
        dst,
        srcAmount,
        minRate,
        destAddr,
        nonce,
        fee,
        status,
        txHash,
        createdAt,
        updatedAt
    )
}