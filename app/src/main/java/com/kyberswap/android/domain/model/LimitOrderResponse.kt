package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.limitorder.LimitOrderResponseEntity


data class LimitOrderResponse(
    val fields: List<String> = listOf(),
    val order: Order = Order(),
    val success: Boolean = false,
    val message: Map<String, List<String>> = mapOf()

) {
    constructor(entity: LimitOrderResponseEntity) : this(
        entity.fields, Order(entity.order), entity.success, entity.message
    )
}