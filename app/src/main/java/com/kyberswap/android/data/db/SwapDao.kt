package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.Swap
import io.reactivex.Flowable

/**
 * Data Access Object for the swaps table.
 */
@Dao
interface SwapDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSwap(swap: Swap)

    @Update
    fun updateSwap(swap: Swap)

    @Query("SELECT * from swaps where walletAddress = :address")
    fun findSwapDataByAddress(address: String): Flowable<Swap>

    @Query("DELETE FROM swaps")
    fun deleteAllSwaps()

    @get:Query("SELECT * FROM swaps")
    val all: Flowable<List<Swap>>
}

