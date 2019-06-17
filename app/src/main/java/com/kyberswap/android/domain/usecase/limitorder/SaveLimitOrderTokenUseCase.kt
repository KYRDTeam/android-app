package com.kyberswap.android.domain.usecase.limitorder

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class SaveLimitOrderTokenUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val limitOrderRepository: LimitOrderRepository
) : CompletableUseCase<SaveLimitOrderTokenUseCase.Param, Any?>(schedulerProvider) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Param): Completable {
        return limitOrderRepository.saveLimitOrder(param)
    }

    class Param(val walletAddress: String, val token: Token, val isSourceToken: Boolean)
}
