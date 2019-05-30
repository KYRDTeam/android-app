package com.kyberswap.android.data.db

import androidx.room.*
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

    @Update
    fun updateTransaction(transaction: Transaction)

    @Update
    fun updateTransactionBatch(transactions: List<Transaction>)

    @Query("DELETE FROM transactions")
    fun deleteAllTransactions()

    @get:Query("SELECT * FROM transactions")
    val all: Flowable<List<Transaction>>

}

