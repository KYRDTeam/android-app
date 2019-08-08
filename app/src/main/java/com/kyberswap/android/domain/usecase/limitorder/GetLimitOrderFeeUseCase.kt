package com.kyberswap.android.domain.usecase.limitorder

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Fee
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class GetLimitOrderFeeUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val limitOrderRepository: LimitOrderRepository
) : FlowableUseCase<GetLimitOrderFeeUseCase.Param, Fee>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: Param): Flowable<Fee> {
        return limitOrderRepository.getLimitOrderFee(param)
    }

    class Param(
        val sourceToken: Token,
        val destToken: Token,
        val sourceAmount: String,
        val destAmount: String,
        val userAddress: String
    )
}
