package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.PassCode
import io.reactivex.Flowable

/**
 * Data Access Object for the pass_codes table.
 */
@Dao
interface PassCodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPassCode(passCode: PassCode)

    @Transaction
    fun createNewPassCode(passCode: PassCode) {
        deleteAllPassCodes()
        insertPassCode(passCode)
    }

    @Update
    fun updatePassCode(passCode: PassCode)

    @Query("SELECT * from pass_codes LIMIT 1")
    fun findPassCodeFlowable(): Flowable<PassCode>

    @Query("SELECT * from pass_codes LIMIT 1")
    fun findPassCode(): PassCode?

    @Query("DELETE FROM pass_codes")
    fun deleteAllPassCodes()

    @get:Query("SELECT * FROM pass_codes")
    val all: Flowable<PassCode>
}

