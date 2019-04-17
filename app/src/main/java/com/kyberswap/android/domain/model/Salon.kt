package com.kyberswap.android.domain.model

import android.os.Parcel
import android.os.Parcelable

data class Salon(
    val id: String,
    val name: String,
    val address: String = "",
    val url: String = ""
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString()

    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(name)
        writeString(address)
        writeString(url)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Salon> = object : Parcelable.Creator<Salon> {
            override fun createFromParcel(source: Parcel): Salon = Salon(source)
            override fun newArray(size: Int): Array<Salon?> = arrayOfNulls(size)
        }
    }
}