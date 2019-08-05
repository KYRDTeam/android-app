package com.kyberswap.android.domain.usecase.limitorder

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.MergeDelayErrorUseCase
import com.kyberswap.android.presentation.main.limitorder.OrdersWrapper
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.Flowables
import java.util.concurrent.TimeUnit
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

.repeatWhen {
            it.delay(15, TimeUnit.SECONDS)

            .retryWhen { throwable ->
                throwable.compose(zipWithFlatMap())
    
    }

    private fun <T> zipWithFlatMap(): FlowableTransformer<T, Long> {
        return FlowableTransformer { flowable ->
            flowable.zipWith(
                Flowable.range(
                    COUNTER_START,
                    ATTEMPTS
                ),
                BiFunction<T, Int, Int> { _: T, u: Int -> u })
                .flatMap { t -> Flowable.timer(t * 5L, TimeUnit.SECONDS) }

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

    companion object {
        private const val COUNTER_START = 1
        private const val ATTEMPTS = 5
    }
}
