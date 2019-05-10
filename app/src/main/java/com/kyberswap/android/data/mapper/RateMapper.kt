package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.rate.RateEntity
import com.kyberswap.android.domain.model.Rate
import javax.inject.Inject

class RateMapper @Inject constructor() {
    fun transform(entity: RateEntity): Rate {
        return Rate(entity)
    }
}