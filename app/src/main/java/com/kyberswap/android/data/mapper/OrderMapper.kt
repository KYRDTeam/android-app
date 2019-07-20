package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.limitorder.CancelledEntity
import com.kyberswap.android.data.api.limitorder.LimitOrderResponseEntity
import com.kyberswap.android.data.api.limitorder.OrderEntity
import com.kyberswap.android.data.api.limitorder.PendingBalancesEntity
import com.kyberswap.android.domain.model.Cancelled
import com.kyberswap.android.domain.model.LimitOrderResponse
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.PendingBalances
import javax.inject.Inject

class OrderMapper @Inject constructor() {
    fun transform(entity: OrderEntity): Order {
        return Order(entity)
    }

    fun transform(entities: List<OrderEntity>): List<Order> {
        return entities.map {
            transform(it)

    }

    fun transform(entity: LimitOrderResponseEntity): LimitOrderResponse {
        return LimitOrderResponse(entity)
    }

    fun transform(entity: CancelledEntity): Cancelled {
        return Cancelled(entity)
    }


    fun transform(entity: PendingBalancesEntity): PendingBalances {
        return PendingBalances(entity)
    }

}