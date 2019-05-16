package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.Rate
import io.reactivex.Flowable


/**
 * Data Access Object for the rates table.
 */
@Dao
interface RateDao {

    @Query("SELECT * FROM rates WHERE source =:source AND dest =:dest LIMIT 1")
    fun getRateForTokenPair(source: String, dest: String): Flowable<Rate>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRate(rate: Rate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRates(rates: List<Rate>)

    @Transaction
    fun updateAll(rates: List<Rate>) {
        deleteAllRates()
        insertRates(rates)
    }

    @Update
    fun updateRate(rate: Rate)

    @Update
    fun updateRates(rates: List<Rate>)

    @Query("DELETE FROM rates")
    fun deleteAllRates()

    @get:Query("SELECT * FROM rates")
    val all: Flowable<List<Rate>>

}


