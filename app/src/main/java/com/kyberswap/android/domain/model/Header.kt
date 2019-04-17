package com.kyberswap.android.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.kyberswap.android.data.api.home.entity.ArticleFeatureEntity

data class Header(
    val type: Type = Type.TOP,
    val label: String? = null,
    val featureId: Long = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Type::class.java.classLoader),
        parcel.readString(),
        parcel.readLong()
    )

    constructor(articleFeatureEntity: ArticleFeatureEntity) : this(
        Type.FEATURE,
        articleFeatureEntity.label,
        articleFeatureEntity.id
    )

    enum class Type : Parcelable {
        TOP, FEATURE;

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(ordinal)


        override fun describeContents(): Int {
            return 0


        companion object CREATOR : Parcelable.Creator<Type> {
            override fun createFromParcel(parcel: Parcel): Type {
                return Type.values()[parcel.readInt()]
    

            override fun newArray(size: Int): Array<Type?> {

                return arrayOfNulls(size)
    

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(type, flags)
        parcel.writeString(label)
        parcel.writeLong(featureId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Header> {
        override fun createFromParcel(parcel: Parcel): Header {
            return Header(parcel)


        override fun newArray(size: Int): Array<Header?> {
            return arrayOfNulls(size)

    }
}
