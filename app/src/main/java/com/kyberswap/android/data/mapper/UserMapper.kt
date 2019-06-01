package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.user.LoginUserEntity
import com.kyberswap.android.data.api.user.RegisterStatusEnity
import com.kyberswap.android.domain.model.LoginUser
import com.kyberswap.android.domain.model.RegisterStatus
import javax.inject.Inject

class UserMapper @Inject constructor() {
    fun transform(entity: RegisterStatusEnity): RegisterStatus {
        return RegisterStatus(entity)
    }

    fun transform(entity: LoginUserEntity): LoginUser {
        return LoginUser(entity)
    }
}