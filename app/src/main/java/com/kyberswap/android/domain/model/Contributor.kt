package com.kyberswap.android.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.kyberswap.android.data.api.home.entity.ContributorEntity

data class Contributor(
    val id: String = "",
    val affiliation: String = "",
    val imageUrl: String = "",
    val jobTitle: String = "",
    val name: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    constructor(contributor: ContributorEntity) : this(
        contributor.id,
        contributor.affiliation,
        contributor.imageUrl,
        contributor.jobTitle,
        contributor.name
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(affiliation)
        parcel.writeString(imageUrl)
        parcel.writeString(jobTitle)
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Contributor> {
        override fun createFromParcel(parcel: Parcel): Contributor {
            return Contributor(parcel)
        }

        override fun newArray(size: Int): Array<Contributor?> {
            return arrayOfNulls(size)
        }
    }
}