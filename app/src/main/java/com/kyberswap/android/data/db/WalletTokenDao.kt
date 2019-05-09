package com.kyberswap.android.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kyberswap.android.domain.model.WalletToken
import io.reactivex.Flowable

@Dao
interface WalletTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(walletToken: WalletToken)

    @Query("SELECT * FROM wallet_token INNER JOIN wallets ON wallet_token.walletAddress LIKE wallets.address WHERE wallet_token.walletAddress LIKE :walletAddress")
    fun getTokensForWallet(walletAddress: String): Flowable<List<WalletToken>>

    @get:Query("SELECT * FROM wallet_token")
    val all: Flowable<List<WalletToken>>

}