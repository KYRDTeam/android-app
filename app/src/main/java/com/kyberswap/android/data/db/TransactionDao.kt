package com.kyberswap.android.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kyberswap.android.domain.model.Transaction
import io.reactivex.Flowable

/**
 * Data Access Object for the transactions table.
 */
@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE hash = :hash")
    fun getTransactionByHash(hash: String): Transaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransactionBatch(transactions: List<Transaction>)

    @androidx.room.Transaction

    fun forceUpdateTransactionBatch(transactions: List<Transaction>, walletAddress: String) {
        deleteTransactions(getCompletedTransactionsList(walletAddress))
        insertTransactionBatch(transactions)
    }

    @androidx.room.Transaction
    fun forceUpdateTransactions(transactions: List<Transaction>) {
        deleteAllTransactions()
        insertTransactionBatch(transactions)
    }

    @Update
    fun updateTransaction(transaction: Transaction)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTransactionList(transactions: List<Transaction>)

    @Update
    fun updateTransactionBatch(transactions: List<Transaction>)

    @Query("DELETE FROM transactions")
    fun deleteAllTransactions()

    @Query("SELECT * FROM transactions WHERE walletAddress = :walletAddress AND transactionStatus != :pending ORDER BY timeStamp DESC LIMIT 1")
    fun getLatestTransaction(
        walletAddress: String, pending: String = Transaction.PENDING_TRANSACTION_STATUS
    ): Transaction?

    @Delete
    fun delete(model: Transaction)

    @Delete
    fun deleteTransactions(transactions: List<Transaction>)

    @get:Query("SELECT * FROM transactions")
    val all: Flowable<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE (walletAddress = :walletAddress OR `from` = :walletAddress OR `to` = :walletAddress) AND transactionStatus != :pending")
    fun getCompletedTransactions(
        walletAddress: String,
        pending: String = Transaction.PENDING_TRANSACTION_STATUS
    ): Flowable<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE (walletAddress = :walletAddress OR `from` = :walletAddress OR `to` = :walletAddress) AND transactionStatus != :pending")
    fun getCompletedTransactionsList(
        walletAddress: String,
        pending: String = Transaction.PENDING_TRANSACTION_STATUS
    ): List<Transaction>

    @Query("SELECT * FROM transactions WHERE hash = :hash COLLATE NOCASE AND transactionStatus = :status")
    fun findTransaction(hash: String, status: String): Transaction?

    @Query("SELECT * FROM transactions WHERE walletAddress = :walletAddress AND transactionStatus = :status")
    fun getTransactionByStatus(walletAddress: String, status: String): Flowable<List<Transaction>>
}

