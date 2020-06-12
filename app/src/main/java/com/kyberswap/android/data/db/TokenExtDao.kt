package com.kyberswap.android.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kyberswap.android.domain.model.TokenExt
import io.reactivex.Flowable

/**
 * Data Access Object for the token_extras table.
 */
@Dao
interface TokenExtDao {

    @Query("SELECT * FROM token_extras WHERE tokenAddress = :tokenAddress")
    fun getTokenExtByAddress(tokenAddress: String): TokenExt?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTokenExt(tokenExt: TokenExt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTokenExtras(tokenExtras: List<TokenExt>)

    @Transaction
    fun batchInsertTokenExtras(tokenExtras: List<TokenExt>) {
        deleteAllTokens()
        insertTokenExtras(tokenExtras)
    }

    @Delete
    fun deleteTokenExt(tokenExt: TokenExt)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTokenExt(tokenExt: TokenExt)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTokenExtras(tokenExtras: List<TokenExt>)

    @Query("DELETE FROM token_extras")
    fun deleteAllTokens()

    @get:Query("SELECT * FROM token_extras")
    val all: Flowable<List<TokenExt>>

    @get:Query("SELECT * FROM token_extras")
    val allTokens: List<TokenExt>

//    @get:Query("SELECT * FROM token_extras WHERE isQuote = 1")
//    val quotedTokens: List<TokenExt>
}

