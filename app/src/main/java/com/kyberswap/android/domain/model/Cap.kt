package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.cap.CapEntity
import java.math.BigDecimal

data class Cap(
    val cap: BigDecimal = BigDecimal.ZERO,
    val kyced: Boolean = false,
    val rich: Boolean = false
) {
    constructor(entity: CapEntity) : this(entity.cap, entity.kyced, entity.rich)

}