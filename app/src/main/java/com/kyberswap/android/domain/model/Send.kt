package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.util.ext.formatDisplayNumber
import com.kyberswap.android.util.ext.rounding
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toDoubleOrDefaultZero
import com.kyberswap.android.util.ext.toNumberFormat
import kotlinx.android.parcel.Parcelize
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger

@Entity(tableName = "sends")
@Parcelize
data class Send(
    @NonNull
    @PrimaryKey
    val walletAddress: String = "",
    @Embedded(prefix = "token_")
    val tokenSource: Token = Token(),
    var sourceAmount: String = "",
    var gasPrice: String = "",
    var gasLimit: String = "",
    @Embedded
    var gas: Gas = Gas(),
    @Embedded(prefix = "contact_")
    var contact: Contact = Contact(),
    @Embedded(prefix = "eth_")
    val ethToken: Token = Token()

) : Parcelable {

    val gasPriceValue: BigDecimal
        get() = this.gasPrice.toBigDecimalOrDefaultZero()

    val estimateSource: String
        get() = if (sourceAmount.toDoubleOrDefaultZero() > 0.0) sourceAmount else "0.001"

    fun isGasPriceChange(other: Send?): Boolean {
        return !this.gasPrice.equals(other?.gasPrice, true)
    }

    fun isGasLimitChange(other: Send?): Boolean {
        return !this.gasLimit.equals(other?.gasLimit, true)
    }


    fun isSameTokenPair(other: Send?): Boolean {
        return this.walletAddress == other?.walletAddress &&
            this.tokenSource.tokenSymbol == other.tokenSource.tokenSymbol &&
            this.tokenSource.currentBalance == other.tokenSource.currentBalance &&
            this.ethToken.currentBalance == other.ethToken.currentBalance
    }

    val amountUnit: BigInteger
        get() = sourceAmount.toBigDecimalOrDefaultZero()
            .multiply(BigDecimal.TEN.pow(tokenSource.tokenDecimal)).toBigInteger()

    val displaySourceAmount: String
        get() = StringBuilder().append(sourceAmount.toNumberFormat()).append(" ")
            .append(tokenSource.tokenSymbol)
            .toString()

    val isSendAll: Boolean
        get() = sourceAmount == tokenSource.currentBalance.rounding().toDisplayNumber()

    val displaySourceAmountUsd: String
        get() = if (tokenSource.rateUsdNow == BigDecimal.ZERO) "" else
            StringBuilder()
                .append("≈ ")
                .append(
                    sourceAmount.toBigDecimalOrDefaultZero().multiply(tokenSource.rateUsdNow)
                        .formatDisplayNumber()
                )
                .append(" USD")
                .toString()

    val displayGasFastFeeEth: String
        get() = Convert.fromWei(
            Convert.toWei(gas.fast.toBigDecimalOrDefaultZero(), Convert.Unit.GWEI)
                .multiply(gasLimit.toBigDecimalOrDefaultZero()), Convert.Unit.ETHER
        ).formatDisplayNumber()

    val displayGasSuperFastFeeEth: String
        get() = Convert.fromWei(
            Convert.toWei(gas.superFast.toBigDecimalOrDefaultZero(), Convert.Unit.GWEI)
                .multiply(gasLimit.toBigDecimalOrDefaultZero()), Convert.Unit.ETHER
        ).formatDisplayNumber()

    val displayGasStandardFeeEth: String
        get() = Convert.fromWei(
            Convert.toWei(gas.standard.toBigDecimalOrDefaultZero(), Convert.Unit.GWEI)
                .multiply(gasLimit.toBigDecimalOrDefaultZero()), Convert.Unit.ETHER
        ).formatDisplayNumber()

    val displayGasLowFeeEth: String
        get() = Convert.fromWei(
            Convert.toWei(gas.low.toBigDecimalOrDefaultZero(), Convert.Unit.GWEI)
                .multiply(gasLimit.toBigDecimalOrDefaultZero()), Convert.Unit.ETHER
        ).formatDisplayNumber()

    val transactionFeeEth: String
        get() = StringBuilder()
            .append("≈ ")
            .append(
                Convert.fromWei(
                    Convert.toWei(
                        gasPrice.toBigDecimalOrDefaultZero(),
                        Convert.Unit.GWEI
                    ).multiply(gasLimit.toBigDecimalOrDefaultZero()), Convert.Unit.ETHER
                ).formatDisplayNumber()
            )
            .append(" ETH")
            .toString()

    val transactionFeeUsd: String
        get() = StringBuilder()
            .append("≈ ")
            .append(
                Convert.fromWei(
                    Convert.toWei(
                        gasPrice.toBigDecimalOrDefaultZero(),
                        Convert.Unit.GWEI
                    ).multiply(gasLimit.toBigDecimalOrDefaultZero()), Convert.Unit.ETHER
                ).multiply(
                    ethToken.rateUsdNow
                ).formatDisplayNumber()
            )
            .append(" USD")
            .toString()


    fun reset() {
        this.sourceAmount = ""
    }

    val insufficientEthBalance: Boolean
        get() = ethToken.currentBalance < gasFeeEth

    val gasFeeEth: BigDecimal
        get() = Convert.fromWei(
            Convert.toWei(gasPrice.toBigDecimalOrDefaultZero(), Convert.Unit.GWEI)
                .multiply(gasLimit.toBigDecimalOrDefaultZero()), Convert.Unit.ETHER
        )

    fun availableAmountForTransfer(
        calAvailableAmount: BigDecimal,
        gasLimit: BigDecimal,
        gasPrice: BigDecimal
    ): BigDecimal {
        return (calAvailableAmount - Convert.fromWei(
            Convert.toWei(gasPrice, Convert.Unit.GWEI)
                .multiply(gasLimit), Convert.Unit.ETHER
        )).max(BigDecimal.ZERO)
    }
}