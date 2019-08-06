package com.kyberswap.android.domain.model

import android.os.Parcelable
import com.kyberswap.android.data.api.gas.GasEntity
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

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

    val superFast: String
        get() = BigDecimal("20").max(fast.toBigDecimalOrDefaultZero()).toDisplayNumber()
}