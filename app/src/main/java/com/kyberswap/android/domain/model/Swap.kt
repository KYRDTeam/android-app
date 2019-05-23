package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.presentation.common.DEFAULT_ROUNDING_NUMBER
import com.kyberswap.android.util.ext.percentage
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.parcel.Parcelize
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

@Entity(tableName = "swaps")
@Parcelize
data class Swap(
    @NonNull
    @PrimaryKey
    val walletAddress: String = "",
    @Embedded(prefix = "source_")
    val tokenSource: Token = Token(),
    @Embedded(prefix = "dest_")
    val tokenDest: Token = Token(),
    var sourceAmount: String = "",
    var destAmount: String = "",
    var expectedRate: String = "",
    var slippageRate: String = "",
    var gasPrice: String = "",
    var gasLimit: String = "",
    var marketRate: String = "",
    var minAcceptedRatePercent: String = ""

) : Parcelable {


    val displayExpectedRate: String
        get() = if (samePair) BigDecimal.ONE.toDisplayNumber() else
            expectedRate.toBigDecimalOrDefaultZero()
                .toDisplayNumber()

    val displaySourceAmount: String
        get() = StringBuilder().append(sourceAmount).append(" ").append(tokenSource.tokenSymbol).toString()

    val displayDestAmount: String
        get() = StringBuilder().append(
            getExpectedDestAmount(
                expectedRate,
                sourceAmount
            ).toDisplayNumber()
        )
            .append(" ")
            .append(tokenDest.tokenSymbol)
            .toString()

    val displayDestAmountUsd: String
        get() = StringBuilder()
            .append("≈ ")
            .append(
                getExpectedDestAmount(
                    expectedRate,
                    sourceAmount
                ).multiply(tokenDest.rateUsdNow).toDisplayNumber()
            )
            .append(" USD")
            .toString()

    val samePair: Boolean
        get() = tokenSource.tokenSymbol == tokenDest.tokenSymbol


    val displayDestRateEthUsd: String
        get() = StringBuilder()
            .append("1 ")
            .append(tokenDest.tokenSymbol)
            .append(" = ")
            .append(tokenDest.rateEthNow.toDisplayNumber() + " ETH")
            .append(" = ")
            .append(tokenDest.rateUsdNow.toDisplayNumber() + " USD")
            .toString()

    val displayGasFee: String
        get() = StringBuilder()
            .append("≈ ")
            .append(
                Convert.fromWei(
                    Convert.toWei(gasPrice.toBigDecimalOrDefaultZero(), Convert.Unit.GWEI)
                        .multiply(gasLimit.toBigDecimalOrDefaultZero()), Convert.Unit.ETHER
                ).toPlainString()
            )
            .append(" ETH")
            .toString()

    val displayMinAcceptedRate: String
        get() = ((BigDecimal.ONE - minAcceptedRatePercent.toBigDecimalOrDefaultZero() / 100.toBigDecimal())
            * expectedRate.toBigDecimalOrDefaultZero())
            .toDisplayNumber()

    val minConversionRate: BigInteger
        get() = ((BigDecimal.ONE - minAcceptedRatePercent.toBigDecimalOrDefaultZero() / 100.toBigDecimal())
            * expectedRate.toBigDecimalOrDefaultZero()).toBigInteger()

    val ratePercentage: String
        get() = expectedRate.percentage(marketRate).toDisplayNumber()

    val ratePercentageAbs: String
        get() = expectedRate.percentage(marketRate).abs().toDisplayNumber()

    fun swapToken(): Swap {
        return Swap(
            this.walletAddress,
            this.tokenDest,
            this.tokenSource,
            this.destAmount,
            this.sourceAmount
//            this.expectedRate,
//            this.slippageRate,
//            this.gasPrice,
//            this.gasLimit,
//            this.percentageRate

        )
    }

    fun reset() {
        this.sourceAmount = ""
        this.destAmount = ""
        this.expectedRate = ""
        this.slippageRate = ""
        this.gasPrice = ""
        this.gasLimit = ""
        this.marketRate = ""
        this.minAcceptedRatePercent = ""
    }

    fun getExpectedDestAmount(expectedRate: String?, amount: String?): BigDecimal {
        return amount.toBigDecimalOrDefaultZero()
            .multiply(expectedRate.toBigDecimalOrDefaultZero())
            .setScale(DEFAULT_ROUNDING_NUMBER, BigDecimal.ROUND_UP)
    }
}