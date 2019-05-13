package com.kyberswap.android.domain.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kyberswap.android.data.api.currencies.TokenCurrencyEntity
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
    var currentBalance: BigDecimal = BigDecimal.ZERO,
    val cgId: String = "",
    @TypeConverters(DataTypeConverter::class)
    val gasApprove: BigDecimal = BigDecimal.ZERO,
    val gasLimit: String = "",
    val listingTime: Long = 0,
    val priority: Boolean = false
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

    constructor(entity: TokenCurrencyEntity) : this(
        tokenSymbol = entity.symbol,
        tokenName = entity.name,
        tokenAddress = entity.address,
        tokenDecimal = entity.decimals,
        cgId = entity.cgId,
        gasApprove = entity.gasApprove,
        gasLimit = entity.gasLimit,
        listingTime = entity.listingTime,
        priority = entity.priority

    )

    fun with(entity: TokenEntity): Token {
        return Token(
            entity.timestamp,
            entity.tokenSymbol,
            entity.tokenName,
            entity.tokenAddress,
            entity.tokenDecimal,
            entity.rateEthNow,
            entity.changeEth24h,
            entity.rateUsdNow,
            entity.changeUsd24h,
            this.currentBalance,
            this.cgId,
            this.gasApprove,
            this.gasLimit,
            this.listingTime,
            this.priority
        )
    }

    fun with(entity: TokenCurrencyEntity): Token {
        return Token(
            this.timestamp,
            this.tokenSymbol,
            this.tokenName,
            this.tokenAddress,
            this.tokenDecimal,
            this.rateEthNow,
            this.changeEth24h,
            this.rateUsdNow,
            this.changeUsd24h,
            this.currentBalance,
            entity.cgId,
            entity.gasApprove,
            entity.gasLimit,
            entity.listingTime,
            entity.priority
        )
    }

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

    val displayGasApprove: String
        get() = gasApprove.toDisplayNumber()

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

    fun change24hStatus(): Int {
        return getChange24hStatus(if (isETH()) changeEth24h else changeUsd24h)
    }

    private fun getChange24hStatus(value: BigDecimal): Int {
        return when {
            value > BigDecimal.ZERO -> UP
            value < BigDecimal.ZERO -> DOWN
            else -> SAME
        }
    }


    companion object {
        const val UP = 1
        const val DOWN = -1
        const val SAME = 0
        const val ETH_SYMBOL = "ETH"
        const val ETH = "ETH"
        const val KNC = "KNC"
    }
}