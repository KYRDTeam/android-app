package com.kyberswap.android.domain.usecase.swap

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.TokenRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetExpectedRateSequentialUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val tokenRepository: TokenRepository
) : SequentialUseCase<GetExpectedRateSequentialUseCase.Param, List<String>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<List<String>> {
        return tokenRepository.getExpectedRate(param)
    }

    class Param(
        val walletAddress: String,
        val tokenSource: Token,
        val tokenDest: Token,
        val srcAmount: String,
        val platformFee: Int
    )
}
