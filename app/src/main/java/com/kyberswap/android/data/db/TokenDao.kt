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

    @Update
    fun updateToken(token: Token)

    @Query("DELETE FROM tokens")
    fun deleteAllTokens()

    @get:Query("SELECT * FROM tokens")
    val all: Flowable<List<Token>>

}

