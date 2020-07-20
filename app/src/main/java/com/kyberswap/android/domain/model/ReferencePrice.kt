package com.kyberswap.android.domain.model


import android.os.Parcelable
import com.kyberswap.android.data.api.rate.ReferencePriceEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReferencePrice(
    val success: Boolean,
    val value: String
) : Parcelable {
    constructor(entity: ReferencePriceEntity) : this(entity.success ?: false, entity.value ?: "")
}