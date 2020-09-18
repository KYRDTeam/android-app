package com.kyberswap.android.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kyberswap.android.domain.model.WalletConnect
import io.reactivex.Flowable


/**
 * Data Access Object for the units table.
 */
@Dao
interface WalletConnectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWalletConnect(walletConnect: WalletConnect)

    @Transaction
    fun updateWalletConnect(walletConnect: WalletConnect) {
        deleteAll()
        insertWalletConnect(walletConnect)
    }

    @Query("DELETE FROM walletconnects")
    fun deleteAll()

    @Query("SELECT * FROM walletconnects LIMIT 1")
    fun getWalletConnect(): WalletConnect?

    @get:Query("SELECT * FROM walletconnects")
    val all: Flowable<WalletConnect>

    @Query("SELECT * from walletconnects where address = :address")
    fun getWalletConnectFlowable(address: String): Flowable<WalletConnect>
}

