package com.kyberswap.android.domain.usecase.transaction

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.repository.TransactionRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val transactionRepository: TransactionRepository
) : CompletableUseCase<DeleteTransactionUseCase.Param, OrderFilter>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Param): Completable {
        return transactionRepository.deleteTransaction(param)
    }

    class Param(val transaction: Transaction)
}
