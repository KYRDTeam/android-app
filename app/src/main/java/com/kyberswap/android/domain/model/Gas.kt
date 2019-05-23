package com.kyberswap.android.domain.model

import android.os.Parcelable
import com.kyberswap.android.data.api.gas.GasEntity
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Gas(
    var fast: String = "",
    var standard: String = "",
    var low: String = "",
    var default: String = ""

) : Parcelable {
    constructor(entity: GasEntity) : this(
        entity.fast, entity.standard, entity.low, entity.default
    )
}