package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.user.UserStatusEnity


data class UserStatus(
    val message: String = "",
    val success: Boolean = false
) {
    constructor(entity: UserStatusEnity) : this(
        entity.message, entity.success
    )
}