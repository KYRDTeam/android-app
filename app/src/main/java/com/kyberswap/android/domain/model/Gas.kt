package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.gas.GasEntity

data class Gas(

    val fast: String = "",
    val standard: String = "",
    val low: String = "",
    val default: String = ""

) {
    constructor(entity: GasEntity) : this(
        entity.fast, entity.standard, entity.low, entity.default
    )
}