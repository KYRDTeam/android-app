package com.kyberswap.android.data.api.limitorder


import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

@JsonAdapter(OrderDeserializer::class)
data class LimitOrderResponseEntity(
    @SerializedName("fields")
    val fields: List<String> = listOf(),
    @SerializedName("orders")
    val order: OrderEntity = OrderEntity(),
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: Map<String, List<String>> = mapOf()

)