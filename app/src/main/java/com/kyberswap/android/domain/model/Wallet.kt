package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kyberswap.android.util.ext.toWalletAddress
import kotlinx.android.parcel.Parcelize
import org.consenlabs.tokencore.wallet.Wallet
import org.jetbrains.annotations.NotNull

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
    var balance: String = "0"
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

        displayBuilder.append(address.substring(0, 5))
            .append("...")
            .append(
                address.substring(
                    if (address.length > 6) {
                        address.length - 6
             else address.length
                )
            )
        return displayBuilder.toString()
    }
}