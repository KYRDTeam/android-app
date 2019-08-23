package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "contacts",
    primaryKeys = ["walletAddress", "address"]
)
data class Contact(
    val walletAddress: String = "",
    val address: String = "",
    val name: String = "",
    val updatedAt: Long = 0
) : Parcelable {
    fun areContentsTheSame(other: Contact): Boolean {
        return this.address == other.address &&
            this.name == other.name
    }

    val displayAddress: String
        get() = StringBuilder()
            .append(if (address.length > 5) address.substring(0, 5) else address.length)
            .append("...")
            .append(
                address.substring(
                    if (address.length > 6) {
                        address.length - 6
                    } else address.length
                )
            ).toString()


    val nameAddressDisplay: String
        get() = "$name - $address"
}