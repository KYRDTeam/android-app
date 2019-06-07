package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.limitorder.FeeEntity
import com.kyberswap.android.domain.model.Fee
import javax.inject.Inject

class FeeMapper @Inject constructor() {
    fun transform(entity: FeeEntity): Fee {
        return Fee(entity)
    }
}