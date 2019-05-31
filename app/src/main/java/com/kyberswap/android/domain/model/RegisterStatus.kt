package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.user.RegisterStatusEnity


data class RegisterStatus(
    val message: String = "",
    val success: Boolean = false
) {
    constructor(entity: RegisterStatusEnity) : this(
        entity.message, entity.success
    )
}