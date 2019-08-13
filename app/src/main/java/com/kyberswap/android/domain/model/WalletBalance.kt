package com.kyberswap.android.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Parcelize
data class WalletBalance(
    val walletAddress: String = "",
    val currentBalance: BigDecimal = BigDecimal.ZERO,
    val isSelected: Boolean = false
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WalletBalance

        if (walletAddress != other.walletAddress) return false

        return true
    }

    override fun hashCode(): Int {
        return walletAddress.hashCode()
    }
}