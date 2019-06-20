package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.Wallet
import io.reactivex.Flowable

/**
 * Data Access Object for the wallets table.
 */
@Dao
interface WalletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWallet(wallet: Wallet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun batchInsertWallets(wallets: List<Wallet>)

    @Update
    fun updateWallet(wallet: Wallet)

    @Query("SELECT * from wallets where address = :address")
    fun loadWalletByAddress(address: String): Flowable<Wallet>

    @Query("SELECT * from wallets where address = :address")
    fun findWalletByAddress(address: String): Wallet

    @Query("DELETE FROM wallets")
    fun deleteAllWallets()

    @get:Query("SELECT * FROM wallets")
    val all: List<Wallet>

    @get:Query("SELECT * FROM wallets")
    val allWalletsFlowable: Flowable<List<Wallet>>
}

