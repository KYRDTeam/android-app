package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.presentation.common.DEFAULT_GAS_LIMIT
import com.kyberswap.android.util.ext.percentage
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toBigIntegerOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.parcel.Parcelize
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

@Entity(tableName = "swaps")
@Parcelize
data class Order(
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
    var gasLimit: String =
        if (tokenSource.gasLimit.toBigIntegerOrDefaultZero()
            == BigInteger.ZERO
        ) DEFAULT_GAS_LIMIT.toString()
        else tokenSource.gasLimit,
    var marketRate: String = "",
    var minAcceptedRatePercent: String = ""

) : Parcelable {

    val hasSamePair: Boolean
        get() = tokenSource.tokenSymbol == tokenDest.tokenSymbol

    val hasTokenPair: Boolean
        get() = tokenSource.tokenSymbol.isNotBlank() && tokenDest.tokenSymbol.isNotBlank()

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
        get() = ((BigDecimal.ONE - minAcceptedRatePercent
            .toBigDecimalOrDefaultZero()
            .divide(100.toBigDecimal())).multiply(
            expectedRate.toBigDecimalOrDefaultZero()
        ))
            .toDisplayNumber()

    val minConversionRate: BigInteger
        get() = Convert.toWei(
            (BigDecimal.ONE - minAcceptedRatePercent
                .toBigDecimalOrDefaultZero()
                .divide(100.toBigDecimal()))
                .multiply(expectedRate.toBigDecimalOrDefaultZero()),
            Convert.Unit.ETHER
        )
            .toBigInteger()

    val ratePercentage: String
        get() = expectedRate.percentage(marketRate).toDisplayNumber()

    val ratePercentageAbs: String
        get() = expectedRate.percentage(marketRate).abs().toDisplayNumber()

    fun swapToken(): Order {
        return Order(
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

    private fun getExpectedDestAmount(expectedRate: String?, amount: String?): BigDecimal {
        return amount.toBigDecimalOrDefaultZero()
            .multiply(expectedRate.toBigDecimalOrDefaultZero())
    }
}