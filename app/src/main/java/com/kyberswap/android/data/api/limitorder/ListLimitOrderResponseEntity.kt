package com.kyberswap.android.data.api.limitorder


import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

@JsonAdapter(OrderListDeserializer::class)
data class ListLimitOrderResponseEntity(
    @SerializedName("fields")
    val fields: List<String> = listOf(),
    @SerializedName("orders")
    val orders: List<OrderEntity> = listOf()
)