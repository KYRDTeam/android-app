package com.kyberswap.android.domain.usecase.swap

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Send
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.SwapRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import org.web3j.protocol.core.methods.response.EthEstimateGas
import javax.inject.Inject

class EstimateTransferGasUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val swapRepository: SwapRepository
) : SequentialUseCase<EstimateTransferGasUseCase.Param, EthEstimateGas>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<EthEstimateGas> {
        return swapRepository.estimateGas(param)
    }

    class Param(
        val wallet: Wallet,
        val send: Send
    )
}
