package com.kyberswap.android.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.kyberswap.android.data.api.home.entity.MetaTagEntity

data class MetaTag(
    val tagName: String,
    val taggedAt: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    constructor(metaTagEntity: MetaTagEntity) : this(metaTagEntity.tagName, metaTagEntity.taggedAt)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(tagName)
        parcel.writeString(taggedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MetaTag> {
        override fun createFromParcel(parcel: Parcel): MetaTag {
            return MetaTag(parcel)
        }

        override fun newArray(size: Int): Array<MetaTag?> {
            return arrayOfNulls(size)
        }
    }
}