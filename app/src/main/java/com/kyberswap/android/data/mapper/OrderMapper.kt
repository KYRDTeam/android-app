package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.limitorder.OrderEntity
import com.kyberswap.android.domain.model.Order
import javax.inject.Inject

class OrderMapper @Inject constructor() {
    fun transform(entity: OrderEntity): Order {
        return Order(entity)
    }
}