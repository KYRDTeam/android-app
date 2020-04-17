package com.kyberswap.android.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kyberswap.android.domain.model.SelectedMarketItem
import io.reactivex.Flowable

/**
 * Data Access Object for the markets table.
 */
@Dao
interface SelectedMarketDao {

    @Query("SELECT * FROM selected_markets WHERE walletAddress = :walletAddress")
    fun getSelectedMarketByWalletAddress(walletAddress: String): SelectedMarketItem?

    @Query("SELECT * FROM selected_markets WHERE walletAddress = :walletAddress LIMIT 1")
    fun getSelectedMarketByWalletAddressFlowable(walletAddress: String): Flowable<SelectedMarketItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSelectedMarket(selectedMarket: SelectedMarketItem)

    @Delete
    fun deleteSelectedMarket(selectedMarket: SelectedMarketItem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSelectedMarket(selectedMarket: SelectedMarketItem)

    @Query("DELETE FROM selected_markets")
    fun deleteAllSelectedMarkets()

    @get:Query("SELECT * FROM selected_markets")
    val all: Flowable<List<SelectedMarketItem>>

    @get:Query("SELECT * FROM selected_markets")
    val allMarkets: List<SelectedMarketItem>
}

