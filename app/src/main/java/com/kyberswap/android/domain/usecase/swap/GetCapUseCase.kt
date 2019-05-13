package com.kyberswap.android.domain.usecase.swap

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Cap
import com.kyberswap.android.domain.repository.SwapRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetCapUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val swapRepository: SwapRepository
) : SequentialUseCase<GetCapUseCase.Param, Cap>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<Cap> {
        return swapRepository.getCap(param)
    }

    class Param(
        val walletAddress: String?
    )


}
