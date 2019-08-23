package com.kyberswap.android.domain.usecase.transaction

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.TransactionFilter
import com.kyberswap.android.domain.repository.TransactionRepository
import com.kyberswap.android.domain.usecase.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class GetTransactionFilterUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val transactionRepository: TransactionRepository
) : FlowableUseCase<GetTransactionFilterUseCase.Param, TransactionFilter>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: Param): Flowable<TransactionFilter> {
        return transactionRepository.getTransactionFilter(param)
    }

    class Param(val walletAddress: String)
}
