package com.kyberswap.android.domain.usecase.token

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.BalanceRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class GetTokenBalanceUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val balanceRepository: BalanceRepository
) : CompletableUseCase<Token, Token>(schedulerProvider) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Token): Completable {
        return balanceRepository.getTokenBalance(param)
    }

}
