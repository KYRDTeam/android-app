package com.kyberswap.android.domain.usecase.swap

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.repository.SwapRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetHintUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val swapRepository: SwapRepository
) : SequentialUseCase<GetHintUseCase.Param, String?>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<String?> {
        return swapRepository.getHint(
            param.srcAddress,
            param.dstAddress,
            param.amount,
            param.isReserveRouting
        )
    }

    class Param(
        val srcAddress: String,
        val dstAddress: String,
        val amount: String,
        val isReserveRouting: Boolean
    )
}
