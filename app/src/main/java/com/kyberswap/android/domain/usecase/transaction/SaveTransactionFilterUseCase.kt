package com.kyberswap.android.domain.usecase.transaction

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.model.TransactionFilter
import com.kyberswap.android.domain.repository.TransactionRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class SaveTransactionFilterUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val transactionRepository: TransactionRepository
) : CompletableUseCase<SaveTransactionFilterUseCase.Param, OrderFilter>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Param): Completable {
        return transactionRepository.saveTransactionFilter(param)
    }

    class Param(val transactionFilter: TransactionFilter)
}
