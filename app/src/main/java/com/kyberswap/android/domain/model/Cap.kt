package com.kyberswap.android.domain.model

import android.os.Parcelable
import com.kyberswap.android.data.api.cap.CapEntity
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Parcelize
data class Cap(
    val cap: BigDecimal = BigDecimal.ZERO,
    val kyced: Boolean = false,
    val rich: Boolean = false
) : Parcelable {
    constructor(entity: CapEntity) : this(entity.cap, entity.kyced, entity.rich)

}