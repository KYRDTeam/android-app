package com.kyberswap.android.data.db

import androidx.room.*
import io.reactivex.Flowable


/**
 * Data Access Object for the units table.
 */
@Dao
interface UnitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUnit(unit: com.kyberswap.android.domain.model.Unit)

    @Transaction
    fun updateUnit(unit: com.kyberswap.android.domain.model.Unit) {
        deleteAll()
        insertUnit(unit)
    }

    @Query("DELETE FROM units")
    fun deleteAll()

    @Query("DELETE FROM units")
    fun deleteAllWallets()

    @get:Query("SELECT * FROM units")
    val unit: Flowable<com.kyberswap.android.domain.model.Unit>
}

