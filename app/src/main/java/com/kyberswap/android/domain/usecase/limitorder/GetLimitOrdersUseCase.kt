package com.kyberswap.android.domain.usecase.limitorder

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.MergeDelayErrorUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class GetLimitOrdersUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val limitOrderRepository: LimitOrderRepository
) : MergeDelayErrorUseCase<GetLimitOrdersUseCase.Param, List<Order>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: Param): Flowable<List<Order>> {
        return limitOrderRepository.getLimitOrders(param)
    }

    class Param(val walletAddress: String)
}
