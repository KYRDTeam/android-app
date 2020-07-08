package com.kyberswap.android.domain.usecase.swap

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.SwapRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class EstimateGasUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val swapRepository: SwapRepository
) : SequentialUseCase<EstimateGasUseCase.Param, BigDecimal>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<BigDecimal> {
        return swapRepository.estimateGas(param)
    }

    class Param(
        val wallet: Wallet,
        val tokenSource: Token,
        val tokenDest: Token,
        val sourceAmount: String,
        val minConversionRate: BigInteger,
        val platformFee: Int
    )
}
