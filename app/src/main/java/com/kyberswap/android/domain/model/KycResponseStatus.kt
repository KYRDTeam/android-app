package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.user.KycResponseStatusEntity


data class KycResponseStatus(
    val success: Boolean = false,
    val reason: List<String> = listOf()
) {
    constructor(entity: KycResponseStatusEntity) : this(
        entity.success, entity.reason
    )
}