package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.parcel.Parcelize
import org.web3j.utils.Convert

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
    var contact: Contact = Contact()

) : Parcelable {

    val displaySourceAmount: String
        get() = StringBuilder().append(sourceAmount).append(" ").append(tokenSource.tokenSymbol).toString()

    val displaySourceAmountUsd: String
        get() = StringBuilder()
            .append("≈ ")
            .append(
                sourceAmount.toBigDecimalOrDefaultZero().multiply(tokenSource.rateUsdNow).toDisplayNumber()
            )
            .append("USD")
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
                ).multiply(
                    tokenSource.rateEthNow
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
                    tokenSource.rateUsdNow
                ).toDisplayNumber()
            )
            .append("USD")
            .toString()


    fun reset() {
        this.sourceAmount = ""
    }
}