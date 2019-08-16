package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.limitorder.FeeEntity


data class Fee(
    val success: Boolean = false,
    val fee: Double = 0.0,
    val discountPercent: Double = 0.0,
    val nonDiscountedFee: Double = 0.0
) {
    constructor(entity: FeeEntity) : this(
        entity.success,
        entity.fee,
        entity.discountPercent,
        entity.nonDiscountedFee
    )
}