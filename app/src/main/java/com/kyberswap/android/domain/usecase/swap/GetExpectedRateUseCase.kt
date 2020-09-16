package com.kyberswap.android.domain.usecase.swap

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.TokenRepository
import com.kyberswap.android.domain.usecase.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class GetExpectedRateUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val tokenRepository: TokenRepository
) : FlowableUseCase<GetExpectedRateUseCase.Param, List<String>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: Param): Flowable<List<String>> {
        return tokenRepository.getExpectedRate(param)
    }

    class Param(
        val walletAddress: String,
        val tokenSource: Token,
        val tokenDest: Token,
        val srcAmount: String,
        val platFormFee: Int,
        val isReserveRouting: Boolean
    )


}
