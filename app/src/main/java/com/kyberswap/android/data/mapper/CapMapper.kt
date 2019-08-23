package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.cap.CapEntity
import com.kyberswap.android.domain.model.Cap
import javax.inject.Inject

class CapMapper @Inject constructor() {
    fun transform(entity: CapEntity): Cap {
        return Cap(entity)
    }
}