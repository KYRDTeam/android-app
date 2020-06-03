package com.kyberswap.android.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kyberswap.android.domain.model.Nonce
import io.reactivex.Flowable

/**
 * Data Access Object for the nonces table.
 */
@Dao
interface NonceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNonce(nonce: Nonce)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateNonce(nonce: Nonce)

    @Query("SELECT * from nonces where walletAddress = :address")
    fun findNonceFlowable(address: String): Flowable<Nonce>

    @Query("SELECT * from nonces where walletAddress = :address LIMIT 1")
    fun findNonce(address: String): Nonce?

    @Query("DELETE FROM nonces")
    fun deleteNonces()

    @Query("DELETE FROM nonces where walletAddress = :address")
    fun deleteNonces(address: String)

    @Delete
    fun delete(nonce: Nonce)

    @get:Query("SELECT * FROM nonces")
    val all: Flowable<List<Nonce>>
}

