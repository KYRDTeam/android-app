package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.alert.AlertEntity
import com.kyberswap.android.data.api.user.LoginUserEntity
import com.kyberswap.android.data.api.user.ResponseStatusEntity
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.LoginUser
import com.kyberswap.android.domain.model.ResponseStatus
import javax.inject.Inject

class UserMapper @Inject constructor() {
    fun transform(entity: ResponseStatusEntity): ResponseStatus {
        return ResponseStatus(entity)
    }

    fun transform(entity: LoginUserEntity): LoginUser {
        return LoginUser(entity)
    }

    fun transform(alerts: List<AlertEntity>): List<Alert> {
        return alerts.map {
            Alert(it)
        }
    }
}