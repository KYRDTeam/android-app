package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.presentation.common.MIN_SUPPORT_SWAP_SOURCE_AMOUNT
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.math.BigInteger

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

    private val _rate: String?
        get() = if (expectedRate.isEmpty()) marketRate else expectedRate

    val combineRate: String?
        get() = _rate.toBigDecimalOrDefaultZero().toDisplayNumber()

    fun swapToken(): LocalLimitOrder {
        return LocalLimitOrder(
            this.userAddr,
            this.tokenDest,
            this.tokenSource
        )
    }

    val displayedSrcAmount: String
        get() = StringBuilder()
            .append(srcAmount)
            .append(" ")
            .append(tokenSource.tokenSymbol)
            .toString()
    val displayedDestAmount: String
        get() = StringBuilder()
            .append(
                srcAmount.toBigDecimalOrDefaultZero().multiply(minRate).toDisplayNumber()
            ).append(" ")
            .append(tokenDest.tokenSymbol)
            .toString()
    val displayReceivedAmount: String
        get() = StringBuilder()
            .append(
                (BigDecimal.ONE - fee).multiply(srcAmount.toBigDecimalOrDefaultZero()).multiply(
                    minRate
                ).toDisplayNumber()
            ).append(" ")
            .append(tokenDest.tokenSymbol)
            .toString()
    val displayedCalculatedRate: String
        get() = StringBuilder()
            .append("(")
            .append(srcAmount)
            .append(" - ")
            .append(fee.multiply(srcAmount.toBigDecimalOrDefaultZero()).toDisplayNumber())
            .append(")")
            .append(tokenSource.tokenSymbol)
            .append(" * ")
            .append(minRate.toDisplayNumber())
            .append(" = ")
            .append(displayReceivedAmount)
            .toString()

    val displayedFee: String
        get() = StringBuilder()
            .append(feeAmount.toDisplayNumber())
            .append(" ")
            .append(tokenSource.tokenSymbol)
            .toString()

    val feeAmount: BigDecimal
        get() = fee.multiply(srcAmount.toBigDecimalOrDefaultZero())

    val feeAmountWithPrecision: BigInteger
        get() = fee.multiply(BigDecimal.TEN.pow(4)).toBigInteger()

    fun getExpectedDestAmount(amount: BigDecimal): BigDecimal {
        return amount.multiply(_rate.toBigDecimalOrDefaultZero())
    }

    val minRateWithPrecision: BigInteger
        get() = minRate.multiply(BigDecimal.TEN.pow(18)).toBigInteger()

    val sourceAmountWithPrecision: BigInteger
        get() = tokenSource.withTokenDecimal(srcAmount.toBigDecimalOrDefaultZero())

    val displayTokenPair: String
        get() = StringBuilder()
            .append(tokenSource.tokenSymbol)
            .append("/")
            .append(tokenDest.tokenSymbol)
            .append(" >= ")
            .append(minRate.toDisplayNumber())
            .toString()

    fun amountTooSmall(sourceAmount: String?): Boolean {
        val amount =
            sourceAmount.toBigDecimalOrDefaultZero().multiply(tokenSource.rateEthNow)
        return if (tokenSource.isETH) {
            amount <= MIN_SUPPORT_SWAP_SOURCE_AMOUNT.toBigDecimal()
        } else {
            amount < MIN_SUPPORT_SWAP_SOURCE_AMOUNT.toBigDecimal()
        }
    }

    val tokenPair: String
        get() = StringBuilder()
            .append(tokenSource.tokenSymbol)
            .append(" ➞ ")
            .append(tokenDest.tokenSymbol)
            .toString()
}