package com.kyberswap.android.domain.usecase.transaction

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.TransactionRepository
import com.kyberswap.android.domain.usecase.MergeDelayErrorUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val transactionRepository: TransactionRepository
) : MergeDelayErrorUseCase<GetTransactionsUseCase.Param, TransactionsData>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: Param): Flowable<TransactionsData> {
        return transactionRepository.fetchAllTransactions(param)
    }

    class Param(
        val wallet: Wallet
    )
}

data class TransactionsData(val transactionList: List<Transaction>, val isLoaded: Boolean)

