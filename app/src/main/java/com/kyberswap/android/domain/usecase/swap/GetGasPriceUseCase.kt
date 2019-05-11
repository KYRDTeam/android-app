package com.kyberswap.android.domain.usecase.swap

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.repository.SwapRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetGasPriceUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val swapRepository: SwapRepository
) : SequentialUseCase<String?, Gas>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: String?): Single<Gas> {
        return swapRepository.getGasPrice()
    }


}
