package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.presentation.common.DEFAULT_ROUNDING_NUMBER
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.parcel.Parcelize
import org.web3j.utils.Convert
import java.math.BigDecimal

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
        get() = expectedRate.toBigDecimalOrDefaultZero()
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
            .append("USD")
            .toString()


    val displayDestRateEthUsd: String
        get() = StringBuilder()
            .append("1")
            .append(tokenDest.tokenSymbol)
            .append(" = ")
            .append(tokenDest.rateEthNow.toDisplayNumber() + "ETH")
            .append(" = ")
            .append(tokenDest.rateUsdNow.toDisplayNumber() + "USD")
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
            .append("ETH")
            .toString()

    val displayMinAcceptedRate: String
        get() = ((1.0 - minAcceptedRatePercent.toBigDecimalOrDefaultZero()
            .toDouble() / 100).toBigDecimal()
            * expectedRate.toBigDecimalOrDefaultZero())
            .toDisplayNumber()


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

    fun getExpectedDestAmount(expectedRate: String?, amount: String?): BigDecimal {
        return amount.toBigDecimalOrDefaultZero()
            .multiply(expectedRate.toBigDecimalOrDefaultZero())
            .setScale(DEFAULT_ROUNDING_NUMBER, BigDecimal.ROUND_UP)
    }
}