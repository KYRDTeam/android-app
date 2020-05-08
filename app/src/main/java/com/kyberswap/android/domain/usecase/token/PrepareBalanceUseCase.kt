package com.kyberswap.android.domain.usecase.token

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.BalanceRepository
import com.kyberswap.android.domain.usecase.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class PrepareBalanceUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val balanceRepository: BalanceRepository
) : FlowableUseCase<PrepareBalanceUseCase.Param, List<Token>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: Param): Flowable<List<Token>> {
        return balanceRepository.getBalance()
    }

    class Param
}
