package com.kyberswap.android.data.api.gas

import java.math.BigDecimal

data class GasLimitEntity(
    val `data`: BigDecimal?,
    val error: Boolean?
)