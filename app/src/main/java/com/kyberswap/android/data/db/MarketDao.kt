package com.kyberswap.android.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kyberswap.android.domain.model.MarketItem
import io.reactivex.Flowable

/**
 * Data Access Object for the markets table.
 */
@Dao
interface MarketDao {

    @Query("SELECT * FROM markets WHERE pair = :pair")
    fun getMarketByPair(pair: String): MarketItem?

    @Query("SELECT * FROM markets WHERE pair = :pair LIMIT 1")
    fun getMarketByPairFlowable(pair: String): Flowable<MarketItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMarket(marketItem: MarketItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMarkets(marketItems: List<MarketItem>)

    @Delete
    fun deleteMarket(marketItem: MarketItem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMarket(marketItem: MarketItem)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMarketItems(items: List<MarketItem>)

    @Query("DELETE FROM markets")
    fun deleteAllMarkets()

    @get:Query("SELECT * FROM markets")
    val all: Flowable<List<MarketItem>>

    @get:Query("SELECT * FROM markets")
    val allMarkets: List<MarketItem>

    @get:Query("SELECT * FROM markets WHERE isFav = 1 ")
    val favMarkets: List<MarketItem>

    @Transaction
    fun updateLatestMarketItem(marketItems: List<MarketItem>) {
        deleteAllMarkets()
        insertMarkets(marketItems)
    }
}

