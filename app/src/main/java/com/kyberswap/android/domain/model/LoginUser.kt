package com.kyberswap.android.domain.model

import com.kyberswap.android.data.api.user.LoginUserEntity

data class LoginUser(
    val authInfo: AuthInfo = AuthInfo(),
    val success: Boolean = false,
    val userInfo: UserInfo = UserInfo(),
    val message: String = "",
    val confirmSignUpRequired: Boolean = false
) {
    constructor(entity: LoginUserEntity) : this(
        AuthInfo(entity.authInfo),
        entity.success,
        UserInfo(entity.userInfo),
        entity.message,
        entity.confirmSignUpRequired

    )
}