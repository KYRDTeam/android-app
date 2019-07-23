package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.TransactionFilter
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.transaction.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface TransactionRepository {
    fun fetchAllTransactions(param: GetTransactionsUseCase.Param): Flowable<List<Transaction>>

    fun fetchTransactionPeriodically(param: GetTransactionsPeriodicallyUseCase.Param): Flowable<List<Transaction>>

    fun fetchERC20TokenTransactions(wallet: Wallet, startBlock: Long = 1): Single<List<Transaction>>

    fun fetchInternalTransactions(address: String, startBlock: Long = 1): Single<List<Transaction>>

    fun fetchNormalTransaction(address: String, startBlock: Long = 1): Single<List<Transaction>>

    fun fetchPendingTransaction(address: String): Flowable<List<Transaction>>

    fun monitorPendingTransactionsPolling(param: MonitorPendingTransactionUseCase.Param): Flowable<List<Transaction>>

    fun saveTransactionFilter(param: SaveTransactionFilterUseCase.Param): Completable

    fun getTransactionFilter(param: GetTransactionFilterUseCase.Param): Flowable<TransactionFilter>


}
