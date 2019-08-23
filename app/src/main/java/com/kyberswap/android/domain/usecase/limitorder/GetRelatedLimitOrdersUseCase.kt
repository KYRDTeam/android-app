package com.kyberswap.android.domain.usecase.limitorder

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class GetRelatedLimitOrdersUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val limitOrderRepository: LimitOrderRepository
) : FlowableUseCase<GetRelatedLimitOrdersUseCase.Param, List<Order>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: Param): Flowable<List<Order>> {
        return limitOrderRepository.getRelatedLimitOrders(param)
    }

    class Param(
        val walletAddress: String,
        val tokenSource: Token,
        val tokenDest: Token,
        val status: String? = null
    )
}
