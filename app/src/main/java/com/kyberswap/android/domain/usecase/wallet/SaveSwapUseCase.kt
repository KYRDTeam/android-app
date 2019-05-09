package com.kyberswap.android.domain.usecase.wallet

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.SwapRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class SaveSwapUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val swapRepository: SwapRepository
) : CompletableUseCase<SaveSwapUseCase.Param>(schedulerProvider) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Param): Completable {
        return swapRepository.saveSwap(param)
    }

    class Param(val swap: Swap)
}
