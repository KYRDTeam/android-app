package com.kyberswap.android.domain.usecase.token

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.BalanceRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class PrepareBalanceUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val balanceRepository: BalanceRepository
) : SequentialUseCase<PrepareBalanceUseCase.Param, List<Token>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<List<Token>> {
        return balanceRepository.getBalance(param)
    }

    class Param(val forceUpdate: Boolean = false)

}
