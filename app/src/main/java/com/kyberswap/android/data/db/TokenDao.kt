package com.kyberswap.android.data.db

import androidx.room.*
import com.kyberswap.android.domain.model.Token
import io.reactivex.Flowable

/**
 * Data Access Object for the tokens table.
 */
@Dao
interface TokenDao {

    @Query("SELECT * FROM tokens WHERE tokenSymbol = :tokenSymbol")
    fun getTokenBySymbol(tokenSymbol: String): Token?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertToken(token: Token)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTokens(tokens: List<Token>)

    @Delete
    fun deleteToken(token: Token)

    @Transaction
    fun updateToken(token: Token) {
        deleteToken(token)
        insertToken(token)
    }

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTokens(tokens: List<Token>)

    @Query("DELETE FROM tokens")
    fun deleteAllTokens()

    @get:Query("SELECT * FROM tokens")
    val all: Flowable<List<Token>>

    @get:Query("SELECT * FROM tokens")
    val allTokens: List<Token>

}

