package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.TypeConverters
import com.kyberswap.android.BuildConfig
import com.kyberswap.android.data.db.BigIntegerDataTypeConverter
import com.kyberswap.android.data.db.DataTypeConverter
import com.kyberswap.android.presentation.common.KEEP_ETH_BALANCE_FOR_GAS
import com.kyberswap.android.util.ext.formatDisplayNumber
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toDoubleSafe
import com.kyberswap.android.util.ext.toNumberFormat
import kotlinx.android.parcel.Parcelize
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Entity(
    tableName = "current_orders",
    primaryKeys = ["userAddr", "type"]
)
@Parcelize
data class LocalLimitOrder(
    val userAddr: String = "",
    @Embedded(prefix = "source_")
    val tokenSource: Token = Token(),
    @Embedded(prefix = "dest_")
    val tokenDest: Token = Token(),
    val srcAmount: String = "",
    val marketRate: String = "",
    val expectedRate: String = "",
    @TypeConverters(DataTypeConverter::class)
    val minRate: BigDecimal = BigDecimal.ZERO,
    val destAddr: String = "",
    val nonce: String = "",
    @TypeConverters(DataTypeConverter::class)
    val fee: BigDecimal = BigDecimal.ZERO,
    val status: String = "",
    val txHash: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    @TypeConverters(BigIntegerDataTypeConverter::class)
    val gasLimit: BigInteger = BigInteger.ZERO,
    val gasPrice: String = "",
    @Embedded(prefix = "eth_")
    val ethToken: Token = Token(),
    @Embedded(prefix = "weth_")
    val wethToken: Token = Token(),
    @TypeConverters(DataTypeConverter::class)
    val transferFee: BigDecimal = BigDecimal.ZERO,
    val type: Int = TYPE_LIMIT_ORDER_V1

) : Parcelable {
    val totalFee: BigDecimal
        get() = fee + transferFee

    val hasSamePair: Boolean
        get() = tokenSource.tokenSymbol == tokenDest.tokenSymbol

    private val _rate: String?
        get() = if (expectedRate.isEmpty()) marketRate else expectedRate

    val isRateTooBig: Boolean
        get() = minRate > _rate.toBigDecimalOrDefaultZero() * 10.toBigDecimal()

    val combineRate: String?
        get() = _rate.toBigDecimalOrDefaultZero().toDisplayNumber()

    val sourceAmountWithoutRounding: String
        get() = if (srcAmount == tokenSource.currentBalance.toDisplayNumber()) tokenSource.sourceAmountWithoutRounding else srcAmount

    val price: String
        get() = if (type == TYPE_BUY)
            BigDecimal.ONE.divide(
                minRate,
                18,
                RoundingMode.CEILING
            ).toDisplayNumber()
        else minRate.toDisplayNumber()

    val displayPrice: String
        get() = StringBuilder().append(
            price
        ).append(" ").append(tokenDest.tokenSymbol).toString()

    val displayMarketRateV2: String
        get() = StringBuilder().append(
            if (type == TYPE_BUY)
                if (marketRate.toDoubleSafe() == 0.0) {
                    ""
                } else
                    BigDecimal.ONE.divide(
                        marketRate.toBigDecimalOrDefaultZero(),
                        18,
                        RoundingMode.CEILING
                    ).formatDisplayNumber()
            else marketRate.toBigDecimalOrDefaultZero().formatDisplayNumber()
        ).toString()

    val displayTotal: String
        get() = if (isBuy) displayedSrcAmountV2 else displayedDestAmountV2

    val displayAmount: String
        get() = if (isBuy) displayedDestAmountV2 else displayedSrcAmountV2

    val displaySource: String
        get() = if (isSell) displayAmount else displayTotal


    fun swapToken(): LocalLimitOrder {
        return LocalLimitOrder(
            this.userAddr,
            this.tokenDest,
            this.tokenSource,
            ethToken = this.ethToken,
            wethToken = this.wethToken
        )
    }

    val pair: String
        get() = if (type == TYPE_BUY) tokenDest.tokenSymbol + "/" + tokenSource.tokenSymbol else
            tokenSource.tokenSymbol + "/" + tokenDest.tokenSymbol

    val isSell: Boolean
        get() = type == TYPE_SELL

    val isBuy: Boolean
        get() = type == TYPE_BUY

    val baseSymbol: String
        get() = if (isSell) tokenSource.tokenSymbol else tokenDest.tokenSymbol

    val quoteSymbol: String
        get() = if (isSell) tokenDest.tokenSymbol else tokenSource.tokenSymbol

    fun isSameTokenPair(other: LocalLimitOrder?): Boolean {
        return this.tokenSource.tokenSymbol == other?.tokenSource?.tokenSymbol &&
            this.tokenDest.tokenSymbol == other.tokenDest.tokenSymbol &&
            this.srcAmount == other.srcAmount &&
            this.marketRate == other.marketRate &&
            this.expectedRate == other.expectedRate &&
            this.nonce == other.nonce &&
            this.gasLimit == other.gasLimit &&
            this.gasPrice == other.gasPrice &&
            this.minRate == other.minRate &&
            this.fee == other.fee &&
            this.transferFee == other.transferFee &&
            this.tokenSource.currentBalance == other.tokenSource.currentBalance &&
            this.tokenDest.currentBalance == other.tokenDest.currentBalance
    }

    fun isSameTokenPairForAddress(other: LocalLimitOrder?): Boolean {
        return this.tokenSource.tokenSymbol == other?.tokenSource?.tokenSymbol &&
            this.tokenDest.tokenSymbol == other.tokenDest.tokenSymbol &&
            this.tokenSource.currentBalance == other.tokenSource.currentBalance &&
            this.tokenDest.currentBalance == other.tokenDest.currentBalance &&
            this.ethToken.currentBalance == other.ethToken.currentBalance &&
            this.wethToken.currentBalance == other.wethToken.currentBalance &&
            this.userAddr == other.userAddr
    }

    fun isSameSourceDestToken(other: LocalLimitOrder?): Boolean {
        return this.tokenSource.tokenSymbol == other?.tokenSource?.tokenSymbol &&
            this.tokenDest.tokenSymbol == other.tokenDest.tokenSymbol &&
            this.userAddr == other.userAddr
    }

    val displayGasFee: String
        get() = StringBuilder()
            .append("≈ ")
            .append(
                gasFeeEth.toPlainString()
            )
            .append(" ETH")
            .toString()

    private val gasFeeEth: BigDecimal
        get() = Convert.fromWei(
            Convert.toWei(gasPrice.toBigDecimalOrDefaultZero(), Convert.Unit.GWEI)
                .multiply(gasLimit.toBigDecimal()), Convert.Unit.ETHER
        )

    val displayMarketRate: String
        get() = marketRate.toBigDecimalOrDefaultZero().formatDisplayNumber()

    val wethBalance: BigDecimal
        get() = wethToken.limitOrderBalance

    val ethBalance: BigDecimal
        get() = ethToken.limitOrderBalance

    val minConvertedAmount: String
        get() = (srcAmount.toBigDecimalOrDefaultZero() - wethToken.currentBalance).toPlainString()

    val displayEthBalance: String
        get() = StringBuilder()
            .append(ethBalance.formatDisplayNumber())
            .append(" ")
            .append(Token.ETH)
            .toString()

    val displayWethBalance: String
        get() = StringBuilder()
            .append(wethBalance.formatDisplayNumber())
            .append(" ")
            .append(Token.ETH)
            .toString()

    val displayedSrcAmount: String
        get() = StringBuilder()
            .append(srcAmount.toNumberFormat())
            .append(" ")
            .append(tokenSource.symbol)
            .toString()

    val displayedSrcAmountV2: String
        get() = StringBuilder()
            .append(srcAmount.toNumberFormat())
            .append(" ")
            .append(tokenSource.tokenSymbol)
            .toString()

    val roundedDestAmount: BigDecimal
        get() {
            val dstAmount = srcAmount.toDoubleSafe().times(minRate.toDouble())
            val iDstAmount = dstAmount.toInt()
            val fractional =
                (dstAmount - iDstAmount).toBigDecimal().toDisplayNumber().toBigDecimal()
            return iDstAmount.toBigDecimal().plus(fractional)
        }
    val displayedDestAmount: String
        get() = StringBuilder()
            .append(
                roundedDestAmount.formatDisplayNumber()
            ).append(" ")
            .append(tokenDest.tokenSymbol)
            .toString()

    val displayedDestAmountV2: String
        get() = StringBuilder()
            .append(
                roundedDestAmount.formatDisplayNumber()
            ).append(" ")
            .append(tokenDest.tokenSymbol)
            .toString()
    val displayReceivedAmount: String
        get() = StringBuilder()
            .append(
                (BigDecimal.ONE - totalFee).multiply(srcAmount.toBigDecimalOrDefaultZero())
                    .multiply(
                        minRate
                    ).formatDisplayNumber()
            ).append(" ")
            .append(tokenDest.symbol)
            .toString()
    val displayedCalculatedRate: String
        get() = StringBuilder()
            .append("(")
            .append(srcAmount)
            .append(" - ")
            .append(totalFee.multiply(srcAmount.toBigDecimalOrDefaultZero()).formatDisplayNumber())
            .append(")")
            .append(tokenSource.symbol)
            .append(" * ")
            .append(minRate.formatDisplayNumber())
            .append(" = ")
            .append(displayReceivedAmount)
            .toString()

    val displayedFee: String
        get() = StringBuilder()
            .append(feeAmount.formatDisplayNumber())
            .append(" ")
            .append(tokenSource.symbol)
            .toString()

    val displayedFeeV2: String
        get() = StringBuilder()
            .append(feeAmount.formatDisplayNumber())
            .append(" ")
            .append(tokenSource.tokenSymbol)
            .toString()

    val feeAmount: BigDecimal
        get() = totalFee.multiply(srcAmount.toBigDecimalOrDefaultZero())

    val feeAmountWithPrecision: BigInteger
        get() = fee.multiply(BigDecimal.TEN.pow(6)).toBigInteger()

    fun getExpectedDestAmount(amount: BigDecimal): BigDecimal {
        return amount.multiply(_rate.toBigDecimalOrDefaultZero())
    }

    val sideTrade: String?
        get() = if (isBuy) Order.SIDE_TRADE_BUY else if (isSell) Order.SIDE_TRADE_SELL else null

    fun getExpectedDestAmount(rate: BigDecimal, amount: BigDecimal): BigDecimal {
        return amount.multiply(rate)
    }

    val minRateWithPrecision: BigInteger
        get() = minRate.multiply(BigDecimal.TEN.pow(18)).toBigInteger()

    val sourceAmountWithPrecision: BigInteger
        get() = tokenSource.withTokenDecimal(sourceAmountWithoutRounding.toBigDecimalOrDefaultZero())

    val displayTokenPair: String
        get() = StringBuilder()
            .append(tokenSource.symbol)
            .append("/")
            .append(tokenDest.symbol)
            .append(" >= ")
            .append(minRate.formatDisplayNumber())
            .toString()

    val minSupportSourceAmount: BigDecimal
        get() = if (BuildConfig.FLAVOR == "dev" || BuildConfig.FLAVOR == "stg") 0.001.toBigDecimal() else 0.1.toBigDecimal()

    fun amountTooSmall(sourceAmount: String?, destAmount: String?): Boolean {
        val amount =
            if (tokenDest.isETHWETH) destAmount.toBigDecimalOrDefaultZero() else
                sourceAmount.toBigDecimalOrDefaultZero()
                    .multiply(tokenSource.rateEthNowOrDefaultValue)
        return if (tokenSource.isETH) {
            amount <= minSupportSourceAmount
        } else {
            amount < minSupportSourceAmount
        }
    }

    fun amountTooBig(sourceAmount: String?): Boolean {
        val amount =
            sourceAmount.toBigDecimalOrDefaultZero().multiply(tokenSource.rateEthNowOrDefaultValue)
        return if (tokenSource.isETH) {
            amount > BigDecimal.TEN
        } else {
            amount >= BigDecimal.TEN
        }
    }

    fun availableAmountForTransfer(
        calAvailableAmount: BigDecimal,
        gasPrice: BigDecimal
    ): BigDecimal {
        return (calAvailableAmount - Convert.fromWei(
            Convert.toWei(gasPrice, Convert.Unit.GWEI)
                .multiply(KEEP_ETH_BALANCE_FOR_GAS), Convert.Unit.ETHER
        )
            ).max(BigDecimal.ZERO)
    }

    val tokenPair: String
        get() = StringBuilder()
            .append(tokenSource.tokenSymbol)
            .append(" ➞ ")
            .append(tokenDest.tokenSymbol)
            .toString()

    companion object {
        const val TYPE_LIMIT_ORDER_V1 = 0
        const val TYPE_BUY = 1
        const val TYPE_SELL = 2
    }
}