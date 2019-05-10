package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.gas.GasEntity
import com.kyberswap.android.domain.model.Gas
import javax.inject.Inject

class GasMapper @Inject constructor() {
    fun transform(entity: GasEntity): Gas {
        return Gas(entity)
    }
}