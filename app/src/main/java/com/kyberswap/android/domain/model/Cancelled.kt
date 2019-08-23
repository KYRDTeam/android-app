package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.limitorder.CancelledEntity


data class Cancelled(
    val cancelled: Boolean = false
) {
    constructor(entity: CancelledEntity) : this(entity.cancelled)
}