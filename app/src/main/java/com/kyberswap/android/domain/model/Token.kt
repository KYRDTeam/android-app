package com.kyberswap.android.domain.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kyberswap.android.data.api.token.TokenEntity
import com.kyberswap.android.data.db.DataTypeConverter
import com.kyberswap.android.util.ext.toDisplayNumber
import java.math.BigDecimal

@Entity(tableName = "tokens")
data class Token(
    val timestamp: Long = 0,
    @NonNull
    @PrimaryKey
    val tokenSymbol: String = "",
    val tokenName: String = "",
    val tokenAddress: String = "",
    val tokenDecimal: Int = 0,
    @TypeConverters(DataTypeConverter::class)
    var rateEthNow: BigDecimal = BigDecimal.ZERO,
    @TypeConverters(DataTypeConverter::class)
    val changeEth24h: BigDecimal = BigDecimal.ZERO,
    @TypeConverters(DataTypeConverter::class)
    var rateUsdNow: BigDecimal = BigDecimal.ZERO,
    @TypeConverters(DataTypeConverter::class)
    val changeUsd24h: BigDecimal = BigDecimal.ZERO,
    @TypeConverters(DataTypeConverter::class)
    var currentBalance: BigDecimal = BigDecimal.ZERO
) {
    constructor(entity: TokenEntity) : this(
        entity.timestamp,
        entity.tokenSymbol,
        entity.tokenName,
        entity.tokenAddress,
        entity.tokenDecimal,
        entity.rateEthNow,
        entity.changeEth24h,
        entity.rateUsdNow,
        entity.changeUsd24h
    )

    val displayRateEthNow: String
        get() = rateEthNow.toDisplayNumber()
    val displayChangeEth24h: String
        get() = changeEth24h.toDisplayNumber()
    val displayRateUsdNow: String
        get() = rateUsdNow.toDisplayNumber()
    val displayChangeUsd24h: String
        get() = changeUsd24h.toDisplayNumber()

    val displayCurrentBalance: String
        get() = currentBalance.toDisplayNumber()

    fun isETH(): Boolean {
        return tokenSymbol.toLowerCase() == ETH_SYMBOL.toLowerCase()
    }

    fun areContentsTheSame(other: Token): Boolean {
        return this.tokenSymbol == other.tokenSymbol &&
            this.currentBalance == other.currentBalance &&
            this.rateEthNow == other.rateEthNow &&
            this.rateUsdNow == other.rateUsdNow &&
            this.changeUsd24h == other.changeUsd24h &&
            this.changeEth24h == other.changeEth24h
    }

    fun isUp(): Boolean {
        return if (isETH()) {
            changeEth24h > BigDecimal.ZERO
 else {
            changeUsd24h > BigDecimal.ZERO

    }

    companion object {
        const val ETH_SYMBOL = "ETH"
    }
}