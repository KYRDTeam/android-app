package com.kyberswap.android.domain.usecase.swap

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.repository.TokenRepository
import com.kyberswap.android.domain.usecase.MergeDelayErrorUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class GetMarketRateUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val tokenRepository: TokenRepository
) : MergeDelayErrorUseCase<GetMarketRateUseCase.Param, String>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: Param): Flowable<String> {
        return tokenRepository.getMarketRate(param)
    }

    class Param(val sourceToken: String, val destToken: String)


}
