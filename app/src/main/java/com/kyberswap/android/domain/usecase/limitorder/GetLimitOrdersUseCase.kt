package com.kyberswap.android.domain.usecase.limitorder

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.MergeDelayErrorUseCase
import com.kyberswap.android.presentation.main.limitorder.OrdersWrapper
import io.reactivex.Flowable
import io.reactivex.rxkotlin.Flowables
import javax.inject.Inject

class GetLimitOrdersUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val limitOrderRepository: LimitOrderRepository
) : MergeDelayErrorUseCase<String?, OrdersWrapper>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: String?): Flowable<OrdersWrapper> {
        return Flowables.zip(
            limitOrderRepository.getOrderFilter(),
            limitOrderRepository.getLimitOrders()
        ) { filter, orders ->
            OrdersWrapper(filterOrders(orders, filter), filter.oldest)


    }


    private fun filterOrders(
        orders: List<Order>,
        orderFilter: OrderFilter
    ): List<Order> {
        return orders
            .filter {
                !orderFilter.unSelectedStatus.map { it.toLowerCase() }.contains(it.status.toLowerCase()) &&
                    !orderFilter.unSelectedPairs.contains(Pair(it.src, it.dst)) &&
                    !orderFilter.unSelectedAddresses.contains(it.userAddr)
    

    }
}
