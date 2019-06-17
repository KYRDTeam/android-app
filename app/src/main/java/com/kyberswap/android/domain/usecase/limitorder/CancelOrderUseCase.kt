package com.kyberswap.android.domain.usecase.limitorder

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Cancelled
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class CancelOrderUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val limitOrderRepository: LimitOrderRepository
) : SequentialUseCase<CancelOrderUseCase.Param, Cancelled>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<Cancelled> {
        return limitOrderRepository.cancelOrder(param)
    }

    class Param(val order: Order)
}
