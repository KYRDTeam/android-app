package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.kyberswap.android.util.ext.toWalletAddress
import com.kyberswap.android.util.views.DateTimeHelper
import kotlinx.android.parcel.Parcelize
import org.consenlabs.tokencore.wallet.Wallet
import org.jetbrains.annotations.NotNull
import org.web3j.utils.Convert
import java.math.BigDecimal

@Parcelize
@Entity(tableName = "wallets")
data class Wallet(
    @PrimaryKey
    @NotNull
    val address: String = "",
    val walletId: String = "",
    val name: String = "",
    val cipher: String = "",
    var isSelected: Boolean = false,
    var unit: String = "USD",
    var balance: String = "0",
    @Embedded
    var cap: Cap = Cap(),
    @Embedded
    var promo: Promo? = Promo()
) :
    Parcelable {
    constructor(wallet: Wallet) : this(
        wallet.address.toWalletAddress(),
        wallet.id,
        wallet.metadata.name
    )

    fun display(): String {
        val displayBuilder = StringBuilder()
        if (name.isNotEmpty()) {
            displayBuilder.append(name).append(" - ")
        }
        displayBuilder.append(address.substring(0, 5))
            .append("...")
            .append(
                address.substring(
                    if (address.length > 6) {
                        address.length - 6
                    } else address.length
                )
            )
        return displayBuilder.toString()
    }

    val displayBalance: String
        get() = if (balance.toBigDecimalOrDefaultZero() > BigDecimal(1E-18)) balance.toBigDecimalOrDefaultZero().toDisplayNumber() else BigDecimal.ZERO.toDisplayNumber()

    val isPromo: Boolean
        get() = promo != null && promo!!.type.isNotEmpty()

    val isPromoPayment: Boolean
        get() = isPromo && Promo.PAYMENT == promo?.type

    val expiredDatePromoCode: String
        get() = DateTimeHelper.displayDate(promo?.expiredDate)


    fun verifyCap(amount: BigDecimal): Boolean {
        return Convert.toWei(amount, Convert.Unit.ETHER) <= Convert.toWei(
            cap.cap,
            Convert.Unit.GWEI
        ) && !cap.rich
    }
}