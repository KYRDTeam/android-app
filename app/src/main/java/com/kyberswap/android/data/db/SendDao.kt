package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.Send
import io.reactivex.Flowable

/**
 * Data Access Object for the sends table.
 */
@Dao
interface SendDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSend(send: Send)

    @Transaction
    fun add(send: Send) {
        deleteAllSends()
        insertSend(send)
    }

    @Update
    fun updateSend(send: Send)

    @Query("SELECT * from sends where walletAddress = :address")
    fun findSendByAddressFlowable(address: String): Flowable<Send>

    @Query("SELECT * from sends where walletAddress = :address LIMIT 1")
    fun findSendByAddress(address: String): Send?

    @Query("DELETE FROM sends")
    fun deleteAllSends()

    @Delete
    fun delete(model: Send)

    @get:Query("SELECT * FROM sends")
    val all: Flowable<List<Send>>
}

