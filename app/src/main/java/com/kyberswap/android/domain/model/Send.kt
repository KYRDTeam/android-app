package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toDoubleOrDefaultZero
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

    val estimateSource: String
        get() = if (sourceAmount.toDoubleOrDefaultZero() > 0.0) sourceAmount else "0.001"

    fun isSameTokenPair(other: Send?): Boolean {
        return this.walletAddress == other?.walletAddress &&
            this.tokenSource.tokenSymbol == other.tokenSource.tokenSymbol &&
            this.tokenSource.currentBalance == other.tokenSource.currentBalance &&
            this.ethToken.currentBalance == other.ethToken.currentBalance
    }

    val amountUnit: BigInteger
        get() = sourceAmount.toBigDecimalOrDefaultZero().multiply(BigDecimal.TEN.pow(tokenSource.tokenDecimal)).toBigInteger()

    val displaySourceAmount: String
        get() = StringBuilder().append(sourceAmount).append(" ").append(tokenSource.tokenSymbol).toString()

    val isSendAll: Boolean
        get() = sourceAmount == tokenSource.currentBalance.toDisplayNumber()

    val displaySourceAmountUsd: String
        get() = if (tokenSource.rateUsdNow == BigDecimal.ZERO) "" else
            StringBuilder()
                .append("≈ ")
                .append(
                    sourceAmount.toBigDecimalOrDefaultZero().multiply(tokenSource.rateUsdNow).toDisplayNumber()
                )
                .append(" USD")
                .toString()

    val transactionFeeEth: String
        get() = StringBuilder()
            .append("≈ ")
            .append(
                Convert.fromWei(
                    Convert.toWei(
                        gasPrice.toBigDecimalOrDefaultZero(),
                        Convert.Unit.GWEI
                    ).multiply(gasLimit.toBigDecimalOrDefaultZero()), Convert.Unit.ETHER
                ).toDisplayNumber()
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
                ).toDisplayNumber()
            )
            .append(" USD")
            .toString()


    fun reset() {
        this.sourceAmount = ""
    }

    val insufficientEthBalance: Boolean
        get() = ethToken.currentBalance < gasFeeEth

    private val gasFeeEth: BigDecimal
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