package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.TransactionFilter
import io.reactivex.Flowable

/**
 * Data Access Object for the transaction_filter table.
 */
@Dao
interface TransactionFilterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransactionFilter(filter: TransactionFilter)

    @Update
    fun updateTrasactionFilter(transactionFilter: TransactionFilter)

    @Query("SELECT * from transaction_filter where walletAddress = :address")
    fun findTransactionFilterByAddressFlowable(address: String): Flowable<TransactionFilter>

    @Query("SELECT * from transaction_filter where walletAddress = :address LIMIT 1")
    fun findTransactionFilterByAddress(address: String): TransactionFilter?

    @Query("DELETE FROM transaction_filter")
    fun deleteAllTransactionsFilters()

    @Delete
    fun delete(model: TransactionFilter)

    @get:Query("SELECT * FROM transaction_filter")
    val all: Flowable<List<TransactionFilter>>
}