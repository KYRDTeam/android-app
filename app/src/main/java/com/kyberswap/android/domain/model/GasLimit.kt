package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.gas.GasLimitEntity
import java.math.BigDecimal

data class GasLimit(
    val `data`: BigDecimal,
    val error: Boolean
) {
    constructor(entity: GasLimitEntity) : this(
        entity.data ?: BigDecimal.ZERO,
        entity.error ?: false
    )
}
