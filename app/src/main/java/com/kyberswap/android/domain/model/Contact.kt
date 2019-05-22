package com.kyberswap.android.domain.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import org.jetbrains.annotations.NotNull

@Parcelize
@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey
    @NotNull
    val address: String = "",
    val name: String = ""
) : Parcelable {
    fun areContentsTheSame(other: Contact): Boolean {
        return this.address == other.address &&
            this.name == other.name
    }
}