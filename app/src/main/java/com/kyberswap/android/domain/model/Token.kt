package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kyberswap.android.data.api.currencies.TokenCurrencyEntity
import com.kyberswap.android.data.api.token.TokenEntity
import com.kyberswap.android.data.db.DataTypeConverter
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.math.BigInteger

@Entity(tableName = "tokens")
@Parcelize
data class Token(
    val timestamp: Long = 0,
    @NonNull
    @PrimaryKey
    val tokenSymbol: String = "",
    val tokenName: String = "",
    val tokenAddress: String = "",
    val tokenDecimal: Int = 0,
    @TypeConverters(DataTypeConverter::class)
    val rateEthNow: BigDecimal = BigDecimal.ZERO,
    @TypeConverters(DataTypeConverter::class)
    val changeEth24h: BigDecimal = BigDecimal.ZERO,
    @TypeConverters(DataTypeConverter::class)
    val rateUsdNow: BigDecimal = BigDecimal.ZERO,
    @TypeConverters(DataTypeConverter::class)
    val changeUsd24h: BigDecimal = BigDecimal.ZERO,
    @TypeConverters(DataTypeConverter::class)
    val currentBalance: BigDecimal = BigDecimal.ZERO,
    val cgId: String = "",
    @TypeConverters(DataTypeConverter::class)
    val gasApprove: BigDecimal = BigDecimal.ZERO,
    val gasLimit: String = "",
    val listingTime: Long = 0,
    val priority: Boolean = false
) : Parcelable {
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

    fun with(entity: TokenCurrencyEntity): Token {
        return Token(
            this.timestamp,
            entity.symbol,
            entity.name,
            entity.address,
            entity.decimals,
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

    fun with(tokenAddress: String): Token {
        return this.copy(tokenAddress = tokenAddress)
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

    val displayCurrentBalanceInEth: String
        get() = StringBuilder()
            .append(if (isETH) "" else "≈ ")
            .append(
                if (currentBalance > BigDecimal.ZERO) currentBalance
                    .multiply(rateEthNow)
                    .toDisplayNumber() else "0"
            )
            .append(" ETH")
            .toString()


    val displayCurrentBalanceInUSD: String
        get() = StringBuilder()
            .append("≈ ")
            .append(
                if (currentBalance > BigDecimal.ZERO) currentBalance
                    .multiply(rateUsdNow)
                    .toDisplayNumber() else "0"
            )
            .append(" USD")
            .toString()


    val displayGasApprove: String
        get() = gasApprove.toDisplayNumber()

    val isETH: Boolean
        get() = tokenSymbol.toLowerCase() == ETH_SYMBOL.toLowerCase()

    val isWETH: Boolean
        get() = tokenSymbol.toLowerCase() == WETH_SYMBOL.toLowerCase()

    val isDAI: Boolean
        get() = tokenSymbol.toLowerCase() == DAI.toLowerCase()


    val isTUSD: Boolean
        get() = tokenSymbol.toLowerCase() == TUSD.toLowerCase()

    val isETHWETH: Boolean
        get() = tokenSymbol.toLowerCase() == ETH_SYMBOL_STAR.toLowerCase()

    fun areContentsTheSame(other: Token): Boolean {
        return this.tokenSymbol == other.tokenSymbol &&
            this.currentBalance == other.currentBalance &&
            this.rateEthNow == other.rateEthNow &&
            this.rateUsdNow == other.rateUsdNow &&
            this.changeUsd24h == other.changeUsd24h &&
            this.changeEth24h == other.changeEth24h
    }

    fun change24hStatus(isEth: Boolean): Int {
        return getChange24hStatus(if (isEth) changeEth24h else changeUsd24h)
    }

    private fun getChange24hStatus(value: BigDecimal): Int {
        return when {
            value > BigDecimal.ZERO -> UP
            value < BigDecimal.ZERO -> DOWN
            else -> SAME
        }
    }

    fun updatePrecision(value: BigInteger): BigInteger {
        return value.divide(BigInteger.TEN.pow(tokenDecimal))
    }

    fun withTokenDecimal(amount: BigDecimal): BigInteger {
        return amount.multiply(BigDecimal.TEN.pow(tokenDecimal)).toBigInteger()
    }

    companion object {
        const val UP = 1
        const val DOWN = -1
        const val SAME = 0
        const val ETH_SYMBOL = "ETH"
        const val ETH_SYMBOL_STAR = "ETH*"
        const val WETH_SYMBOL = "WETH"
        const val ETH_NAME = "Ethereum"
        const val ETH_DECIMAL = 18
        const val ETH = "ETH"
        const val KNC = "KNC"
        const val DAI = "DAI"
        const val TUSD = "TUSD"
    }
}