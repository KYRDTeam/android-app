package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.limitorder.FeeEntity


data class Fee(
    val fee: Double = 0.0,
    val success: Boolean = false
) {
    constructor(entity: FeeEntity) : this(entity.fee, entity.success)
}