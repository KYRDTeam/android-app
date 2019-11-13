package com.kyberswap.android.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kyberswap.android.domain.model.Token
import io.reactivex.Flowable

/**
 * Data Access Object for the tokens table.
 */
@Dao
interface TokenDao {

    @Query("SELECT * FROM tokens WHERE tokenSymbol = :tokenSymbol")
    fun getTokenBySymbol(tokenSymbol: String): Token?

    @Query("SELECT * FROM tokens WHERE tokenSymbol = :tokenSymbol")
    fun getAllTokenBySymbol(tokenSymbol: String): List<Token>

    @Query("SELECT * FROM tokens WHERE tokenAddress = :tokenAddress")
    fun getTokenByAddress(tokenAddress: String): Token?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertToken(token: Token)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTokens(tokens: List<Token>)

    @Delete
    fun deleteToken(token: Token)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateToken(token: Token)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTokens(tokens: List<Token>)

    @Query("DELETE FROM tokens")
    fun deleteAllTokens()

    @get:Query("SELECT * FROM tokens")
    val all: Flowable<List<Token>>

    @get:Query("SELECT * FROM tokens where isOther = 1 ")
    val others: Flowable<List<Token>>

    @get:Query("SELECT * FROM tokens where isOther = 1 ")
    val otherTokens: List<Token>

    @get:Query("SELECT * FROM tokens")
    val allTokens: List<Token>
}

