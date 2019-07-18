package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.PendingBalances
import io.reactivex.Flowable

/**
 * Data Access Object for the pending_balances table.
 */
@Dao
interface PendingBalancesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPendingBalances(pendingBalances: PendingBalances)

    @Transaction
    fun createNewPendingBalances(pendingBalances: PendingBalances) {
        deleteAllPendingBalances()
        insertPendingBalances(pendingBalances)
    }

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updatePendingBalances(pendingBalances: PendingBalances)

    @Query("SELECT * from pending_balances LIMIT 1")
    fun findPendingBalancesFlowable(): Flowable<PendingBalances>

    @Query("SELECT * from pending_balances LIMIT 1")
    fun findPendingBalances(): PendingBalances?

    @Query("DELETE FROM pending_balances")
    fun deleteAllPendingBalances()

    @get:Query("SELECT * FROM pending_balances")
    val all: Flowable<PendingBalances>
}

