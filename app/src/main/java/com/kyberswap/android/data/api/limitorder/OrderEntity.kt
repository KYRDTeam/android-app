package com.kyberswap.android.data.api.limitorder


import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class OrderEntity(
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("addr")
    val userAddr: String = "",
    @SerializedName("src")
    val src: String = "",
    @SerializedName("dst")
    val dst: String = "",
    @SerializedName("src_amount")
    val srcAmount: BigDecimal = BigDecimal.ZERO,
    @SerializedName("min_rate")
    val minRate: BigDecimal = BigDecimal.ZERO,
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

fun JsonObject.toMessage(): Map<String, List<String>> {
    val entries = entrySet()
    val map = mutableMapOf<String, List<String>>()
    entries.forEach { entry ->
        map[entry.key] = entry.value.asJsonArray.toList().map {
            it.asString
        }
    }
    return map
}

fun JsonArray.toOrderEntity(jsonArray: JsonArray): OrderEntity {
    val keys = jsonArray.toList().map {
        it.asString
    }

    val id = if (get(keys.indexOf("id")).isJsonNull) {
        0
    } else {
        get(keys.indexOf("id"))?.asLong ?: 0
    }


    val userAddr =
        if (get(keys.indexOf("addr")).isJsonNull) "" else get(keys.indexOf("addr"))?.asString ?: ""
    val src =
        if (get(keys.indexOf("src")).isJsonNull) "" else get(keys.indexOf("src"))?.asString ?: ""
    val dst =
        if (get(keys.indexOf("dst")).isJsonNull) "" else get(keys.indexOf("dst"))?.asString ?: ""
    val srcAmount =
        get(keys.indexOf("src_amount"))?.asBigDecimal ?: BigDecimal.ZERO
    val minRate =
        if (get(keys.indexOf("min_rate")).isJsonNull) BigDecimal.ZERO else get(keys.indexOf("min_rate"))?.asBigDecimal
            ?: BigDecimal.ZERO
    val nonce =
        if (get(keys.indexOf("nonce")).isJsonNull) "" else get(keys.indexOf("nonce"))?.asString
            ?: ""
    val fee =
        if (get(keys.indexOf("fee")).isJsonNull) BigDecimal.ZERO else get(keys.indexOf("fee"))?.asBigDecimal
            ?: BigDecimal.ZERO
    val status =
        if (get(keys.indexOf("status")).isJsonNull) "" else get(keys.indexOf("status"))?.asString
            ?: ""
    val txHash = if (get(keys.indexOf("tx_hash")).isJsonNull) {
        ""
    } else {
        get(keys.indexOf("tx_hash"))?.asString ?: ""
    }
    val createdAt =
        if (get(keys.indexOf("created_at")).isJsonNull) 0 else get(keys.indexOf("created_at"))?.asLong
            ?: 0
    val updatedAt =
        if (get(keys.indexOf("updated_at")).isJsonNull) 0 else get(keys.indexOf("updated_at"))?.asLong
            ?: 0

    return OrderEntity(
        id,
        userAddr,
        src,
        dst,
        srcAmount,
        minRate,
        nonce,
        fee,
        status,
        txHash,
        createdAt,
        updatedAt
    )
}