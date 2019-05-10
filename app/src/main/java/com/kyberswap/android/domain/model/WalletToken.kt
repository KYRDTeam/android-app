package com.kyberswap.android.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.TypeConverters
import com.kyberswap.android.data.db.DataTypeConverter
import java.math.BigDecimal


@Entity(
    tableName = "wallet_token",
    primaryKeys = ["walletAddress", "tokenSymbol"],
    foreignKeys = [
        ForeignKey(
            entity = Wallet::class,
            parentColumns = arrayOf("address"),
            childColumns = arrayOf("walletAddress")
        ),
        ForeignKey(
            entity = Token::class,
            parentColumns = arrayOf("tokenSymbol"),
            childColumns = arrayOf("tokenSymbol")
        )]
)
data class WalletToken(
    val walletAddress: String,
    val tokenSymbol: String,
    @TypeConverters(DataTypeConverter::class)
    var currentBalance: BigDecimal = BigDecimal.ZERO

)
