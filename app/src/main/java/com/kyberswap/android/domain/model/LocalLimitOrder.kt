package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Entity(tableName = "current_orders")
@Parcelize
data class LocalLimitOrder(
    @PrimaryKey
    val userAddr: String = "",
    @Embedded(prefix = "source_")
    val tokenSource: Token = Token(),
    @Embedded(prefix = "dest_")
    val tokenDest: Token = Token(),
    val srcAmount: String = "",
    var marketRate: String = "",
    var expectedRate: String = "",
    val minRate: BigDecimal = BigDecimal.ZERO,
    val destAddr: String = "",
    val nonce: String = "",
    val fee: BigDecimal = BigDecimal.ZERO,
    val status: String = "",
    val txHash: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0

) : Parcelable {
    val hasSamePair: Boolean
        get() = tokenSource.tokenSymbol == tokenDest.tokenSymbol


    fun swapToken(): LocalLimitOrder {
        return LocalLimitOrder(
            this.userAddr,
            this.tokenDest,
            this.tokenSource
        )
    }

    val tokenPair: String
        get() = StringBuilder()
            .append(tokenSource.tokenSymbol)
            .append(" ➞ ")
            .append(tokenDest.tokenSymbol)
            .toString()
}