package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.gas.GasEntity
import com.kyberswap.android.data.api.gas.GasLimitEntity
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.model.GasLimit
import javax.inject.Inject

class GasMapper @Inject constructor() {
    fun transform(entity: GasEntity): Gas {
        return Gas(entity)
    }

    fun transform(entity: GasLimitEntity): GasLimit {
        return GasLimit(entity)
    }
}