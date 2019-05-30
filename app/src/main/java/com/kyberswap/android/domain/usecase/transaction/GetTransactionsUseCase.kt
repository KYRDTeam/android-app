package com.kyberswap.android.domain.usecase.transaction

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.repository.TransactionRepository
import com.kyberswap.android.domain.usecase.MergeDelayErrorUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val transactionRepository: TransactionRepository
) : MergeDelayErrorUseCase<String, List<Transaction>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: String): Flowable<List<Transaction>> {
        return transactionRepository.fetchAllTransactions(param)
    }
}
