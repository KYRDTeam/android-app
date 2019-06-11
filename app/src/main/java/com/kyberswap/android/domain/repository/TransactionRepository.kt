package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.usecase.transaction.GetTransactionsUseCase
import io.reactivex.Flowable
import io.reactivex.Single

interface TransactionRepository {
    fun fetchAllTransactions(param: GetTransactionsUseCase.Param): Flowable<List<Transaction>>
    fun fetchERC20TokenTransactions(address: String): Single<List<Transaction>>
    fun fetchInternalTransactions(address: String): Single<List<Transaction>>
    fun fetchNormalTransaction(address: String): Single<List<Transaction>>
    fun fetchPendingTransaction(address: String): Flowable<List<Transaction>>
    fun monitorPendingTransactionsPolling(transactions: List<Transaction>): Flowable<List<Transaction>>
}
