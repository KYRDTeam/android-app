package com.kyberswap.android.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.consenlabs.tokencore.wallet.Wallet

@Parcelize
data class Wallet(
    val address: String = "0x2262d4f6312805851e3b27c40db2c7282e6e4a49",
    val name: String = "Satoshi Nakamoto",
    var isSelected: Boolean = false
) :
    Parcelable {
    constructor(wallet: Wallet) : this(wallet.address, wallet.metadata.name)
}