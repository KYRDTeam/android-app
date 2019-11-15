package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.user.ResponseStatusEntity


data class ResponseStatus(
    val message: String = "",
    val success: Boolean = false,
    val hash: String = ""
) {
    constructor(entity: ResponseStatusEntity) : this(
        entity.message, entity.success, entity.hash
    )
}