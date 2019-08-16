package com.kyberswap.android.domain.usecase.send

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.ResponseStatus
import com.kyberswap.android.domain.model.Send
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.SwapRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class TransferTokenUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val swapRepository: SwapRepository
) : SequentialUseCase<TransferTokenUseCase.Param, ResponseStatus>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<ResponseStatus> {
        return swapRepository.transferToken(param)
    }

    class Param(val wallet: Wallet, val send: Send)
}
