package com.kyberswap.android.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Wallet(val address: String) : Parcelable {

    fun sameAddress(address: String): Boolean {
        return this.address == address
    }

}