package com.kyberswap.android.domain.usecase.token

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.BalanceRepository
import com.kyberswap.android.domain.usecase.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class GetTokenUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val balanceRepository: BalanceRepository
) : FlowableUseCase<String?, List<Token>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: String?): Flowable<List<Token>> {
        return balanceRepository.getLocalTokens().map {
            it.filter { token ->
                token.isListed && !token.isOther
            }
        }
    }
}
