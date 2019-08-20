package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.kyberswap.android.presentation.common.DEFAULT_GAS_LIMIT
import com.kyberswap.android.presentation.common.MIN_SUPPORT_SWAP_SOURCE_AMOUNT
import com.kyberswap.android.presentation.common.calculateDefaultGasLimit
import com.kyberswap.android.util.ext.percentage
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toBigIntegerOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toDoubleOrDefaultZero
import kotlinx.android.parcel.Parcelize
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Entity(tableName = "swaps")
@Parcelize
data class Swap(
    @NonNull
    @PrimaryKey
    var walletAddress: String = "",
    @Embedded(prefix = "source_")
    var tokenSource: Token = Token(),
    @Embedded(prefix = "dest_")
    var tokenDest: Token = Token(),
    var sourceAmount: String = "",
    var destAmount: String = "",
    var expectedRate: String = "",
    var slippageRate: String = "",
    var gasPrice: String = "",
    var gasLimit: String =
        if (tokenSource.gasLimit.toBigIntegerOrDefaultZero()
            == BigInteger.ZERO
        ) DEFAULT_GAS_LIMIT.toString()
        else tokenSource.gasLimit,
    var marketRate: String = "",
    var minAcceptedRatePercent: String = "",
    @Embedded(prefix = "gas_")
    var gas: Gas = Gas(),
    @Ignore
    var ethToken: Token = Token()
) : Parcelable {

    val allETHBalanceGasLimit: BigInteger
        get() {
            return calculateDefaultGasLimit(tokenSource, tokenDest) + 10_000.toBigInteger()
        }

    val currentExpectedDestAmount: BigDecimal
        get() = getExpectedDestAmount(sourceAmount.toBigDecimalOrDefaultZero())


    constructor(limitOrder: LocalLimitOrder, minConvertedAmount: BigDecimal) : this(
        limitOrder.userAddr,
        limitOrder.ethToken,
        limitOrder.wethToken,
        minConvertedAmount.stripTrailingZeros().toPlainString(),
        "",
        BigDecimal.ONE.toDisplayNumber(),
        "",
        limitOrder.gasPrice,
        if (limitOrder.gasLimit > BigInteger.ZERO) limitOrder.gasLimit.toString()
        else if (limitOrder.ethToken.gasLimit.toBigIntegerOrDefaultZero()
            == BigInteger.ZERO
        ) DEFAULT_GAS_LIMIT.toString()
        else limitOrder.ethToken.gasLimit,
        BigDecimal.ONE.toDisplayNumber(),
        3.toString()
    )

    fun isSameTokenPair(other: Swap?): Boolean {
        return this.walletAddress == other?.walletAddress &&
            this.tokenSource.tokenSymbol == other.tokenSource.tokenSymbol &&
            this.tokenDest.tokenSymbol == other.tokenDest.tokenSymbol &&
            this.tokenSource.currentBalance == other.tokenSource.currentBalance &&
            this.tokenDest.currentBalance == other.tokenDest.currentBalance &&
            this.ethToken.currentBalance == other.ethToken.currentBalance
    }

    val isSwapAll: Boolean
        get() = sourceAmount == tokenSource.currentBalance.toDisplayNumber()


    fun availableAmountForSwap(
        calAvailableAmount: BigDecimal,
        gasLimit: BigDecimal,
        gasPrice: BigDecimal
    ): BigDecimal {
        return (calAvailableAmount - Convert.fromWei(
            Convert.toWei(gasPrice, Convert.Unit.GWEI)
                .multiply(gasLimit), Convert.Unit.ETHER
        )).max(BigDecimal.ZERO)
    }


    val defaultGasLimit: String
        get() = if (tokenSource.gasLimit.toBigIntegerOrDefaultZero()
            == BigInteger.ZERO
        ) DEFAULT_GAS_LIMIT.toString()
        else tokenSource.gasLimit

    val rate: String?
        get() = if (expectedRate.isEmpty()) marketRate else expectedRate

    val combineRate: String?
        get() = rate.toBigDecimalOrDefaultZero().toDisplayNumber()

    val hasSamePair: Boolean
        get() = tokenSource.tokenSymbol == tokenDest.tokenSymbol

    val hasTokenPair: Boolean
        get() = tokenSource.tokenSymbol.isNotBlank() && tokenDest.tokenSymbol.isNotBlank()

    val displaySourceAmount: String
        get() = StringBuilder().append(sourceAmount.toBigDecimalOrDefaultZero().toDisplayNumber()).append(
            " "
        ).append(tokenSource.tokenSymbol).toString()

    val displayDestAmount: String
        get() = StringBuilder().append(
            getExpectedAmount(
                expectedRate,
                sourceAmount
            ).stripTrailingZeros().toDisplayNumber()
        )
            .append(" ")
            .append(tokenDest.tokenSymbol)
            .toString()

    val warning: Boolean
        get() = sourceAmount.isNotEmpty() && expectedRate.isNotEmpty()

    val displaySourceToDestAmount: String
        get() = StringBuilder().append("1 ")
            .append(tokenSource.tokenSymbol)
            .append(" = ")
            .append(
                getExpectedAmount(
                    expectedRate,
                    1.toString()
                ).toDisplayNumber()
            )
            .append(" ")
            .append(tokenDest.tokenSymbol)
            .append(" = ")
            .append(
                getExpectedAmount(
                    expectedRate,
                    1.toString()
                ).multiply(tokenDest.rateUsdNow).toDisplayNumber()
            )
            .append(" USD")
            .toString()


    val displayDestAmountUsd: String
        get() = StringBuilder()
            .append("≈ ")
            .append(
                getExpectedAmount(
                    expectedRate,
                    sourceAmount
                ).multiply(tokenDest.rateUsdNow).toDisplayNumber()
            )
            .append(" USD")
            .toString()

    val displayRateConversion: String
        get() = StringBuilder()
            .append("1 ")
            .append(tokenSource.tokenSymbol)
            .append(" = ")
            .append(getExpectedAmount(expectedRate, 1.toString()).toDisplayNumber())
            .append(" ")
            .append(tokenDest.tokenSymbol)
            .append(" = ")
            .append(
                tokenSource.rateUsdNow.toDisplayNumber()
            )
            .append(" USD")
            .toString()

    val sourceSymbol: String
        get() = tokenSource.tokenSymbol

    val destSymbol: String
        get() = tokenDest.tokenSymbol


    val displayDestRateEthUsd: String
        get() = StringBuilder()
            .append("1 ")
            .append(tokenDest.tokenSymbol)
            .append(" = ")
            .append(tokenDest.rateEthNow.toDisplayNumber() + " ETH")
            .append(" = ")
            .append(tokenDest.rateUsdNow.toDisplayNumber() + " USD")
            .toString()

    private val gasFeeEth: BigDecimal
        get() = Convert.fromWei(
            Convert.toWei(gasPrice.toBigDecimalOrDefaultZero(), Convert.Unit.GWEI)
                .multiply(gasLimit.toBigDecimalOrDefaultZero()), Convert.Unit.ETHER
        )

    private val gasFeeUsd: BigDecimal
        get() = gasFeeEth.divide(
            tokenSource.rateEthNow,
            18,
            RoundingMode.UP
        ).multiply(tokenSource.rateUsdNow)

    val displayGasFee: String
        get() = StringBuilder()
            .append("≈ ")
            .append(
                gasFeeEth.toDisplayNumber()
            )
            .append(" ETH")
            .toString()

    val displayGasFeeUsd: String
        get() = StringBuilder()
            .append("≈ ")
            .append(
                gasFeeUsd.toDisplayNumber()
            )
            .append(" USD")
            .toString()

    val displayMinAcceptedRate: String
        get() = ((BigDecimal.ONE - minAcceptedRatePercent
            .toBigDecimalOrDefaultZero()
            .divide(
                100.toBigDecimal(),
                18, RoundingMode.UP
            )).multiply(
            expectedRate.toBigDecimalOrDefaultZero()
        ))
            .toDisplayNumber()

    val minConversionRate: BigInteger
        get() = Convert.toWei(
            (BigDecimal.ONE - minAcceptedRatePercent
                .toBigDecimalOrDefaultZero()
                .divide(100.toBigDecimal(), 18, RoundingMode.UP))
                .multiply(expectedRate.toBigDecimalOrDefaultZero()),
            Convert.Unit.ETHER
        )
            .toBigInteger()

    val ratePercentage: String
        get() = if (sourceAmount.isEmpty()) 0.toString() else expectedRate.percentage(
            marketRate
        ).toDisplayNumber()

    val ratePercentageAbs: String
        get() = expectedRate.percentage(marketRate).abs().toDisplayNumber()


    fun swapToken(): Swap {
        return Swap(
            this.walletAddress,
            this.tokenDest,
            this.tokenSource,
            "",
            "",
            if (tokenSource.rateEthNow.toDouble() != 0.0)
                this.tokenDest.rateEthNow.toDouble().div(
                    tokenSource.rateEthNow.toDouble()
                ).toBigDecimal().toDisplayNumber()
            else 0.toString()
        )
    }

    fun amountTooSmall(sourceAmount: String?): Boolean {
        val amount =
            sourceAmount.toBigDecimalOrDefaultZero().multiply(tokenSource.rateEthNow)
        return amount < MIN_SUPPORT_SWAP_SOURCE_AMOUNT.toBigDecimal()
    }

    val equivalentETHWithPrecision: BigDecimal
        get() {
            return tokenSource.withTokenDecimal(
                sourceAmount.toBigDecimalOrDefaultZero()
            ).toBigDecimal().multiply(
                tokenSource.rateEthNow
            )

        }

    val insufficientEthBalance: Boolean
        get() {
            return ethToken.currentBalance < gasFeeEth
        }

    fun getDefaultSourceAmount(ethAmount: String): BigDecimal {
        return if (tokenSource.rateEthNow == BigDecimal.ZERO) BigDecimal.ZERO
        else ethAmount.toBigDecimalOrDefaultZero().divide(
            tokenSource.rateEthNow,
            18,
            RoundingMode.UP
        )
    }

    fun reset() {
        this.sourceAmount = ""
        this.destAmount = ""
        this.expectedRate = ""
        this.slippageRate = ""
        this.gasLimit = ""
        this.marketRate = ""
        this.minAcceptedRatePercent = ""
    }

    fun getExpectedAmount(expectedRate: String?, amount: String?): BigDecimal {
        return amount.toBigDecimalOrDefaultZero()
            .multiply(expectedRate.toBigDecimalOrDefaultZero())
    }

    fun getExpectedDestAmount(amount: BigDecimal): BigDecimal {
        return amount.multiply(rate.toBigDecimalOrDefaultZero())
    }

    fun getExpectedDestUsdAmount(amount: BigDecimal, rateUsdNow: BigDecimal): BigDecimal {
        return getExpectedDestAmount(amount)
            .multiply(rateUsdNow)
            .setScale(2, RoundingMode.UP)
    }

    fun rateThreshold(customRate: String): String {
        return (1.toDouble() - customRate.toDoubleOrDefaultZero() / 100.toDouble())
            .toBigDecimal()
            .multiply(rate.toBigDecimalOrDefaultZero())
            .toDisplayNumber()
    }
}