package com.kyberswap.android.domain.usecase.swap

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.EstimateAmount
import com.kyberswap.android.domain.repository.SwapRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class EstimateAmountUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val swapRepository: SwapRepository
) : SequentialUseCase<EstimateAmountUseCase.Param, EstimateAmount>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<EstimateAmount> {
        return swapRepository.estimateAmount(param)
    }

    class Param(
        val source: String,
        val dest: String,
        val destAmount: String
    )


}
