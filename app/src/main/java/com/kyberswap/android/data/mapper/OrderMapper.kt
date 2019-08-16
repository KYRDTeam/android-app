package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.limitorder.*
import com.kyberswap.android.domain.model.*
import javax.inject.Inject

class OrderMapper @Inject constructor() {
    fun transform(entity: OrderEntity): Order {
        return Order(entity)
    }

    fun transform(entities: List<OrderEntity>): List<Order> {
        return entities.map {
            transform(it)
        }
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

    fun transform(entity: EligibleAddressEntity): EligibleAddress {
        return EligibleAddress(entity)
    }

}