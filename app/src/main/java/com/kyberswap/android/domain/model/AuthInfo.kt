package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.user.AuthInfoEntity


data class AuthInfo(
    val authToken: String = "",
    val expirationTime: String = "",
    val refreshToken: String = ""
) {
    constructor(entity: AuthInfoEntity) : this(
        entity.authToken, entity.expirationTime, entity.refreshToken
    )
}