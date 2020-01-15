package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.user.DataTransferStatusEntity


data class DataTransferStatus(
    val message: String = "",
    val success: Boolean = false
) {
    constructor(entity: DataTransferStatusEntity) : this(
        entity.message, entity.success
    )
}