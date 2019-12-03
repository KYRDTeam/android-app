package com.kyberswap.android.data.mapper

import com.kyberswap.android.data.api.alert.AlertEntity
import com.kyberswap.android.data.api.alert.AlertMethodsResponseEntity
import com.kyberswap.android.data.api.token.QuoteAmountEntity
import com.kyberswap.android.data.api.user.KycResponseStatusEntity
import com.kyberswap.android.data.api.user.LoginUserEntity
import com.kyberswap.android.data.api.user.ResponseStatusEntity
import com.kyberswap.android.data.api.user.UserInfoEntity
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.model.AlertMethodsResponse
import com.kyberswap.android.domain.model.KycResponseStatus
import com.kyberswap.android.domain.model.LoginUser
import com.kyberswap.android.domain.model.QuoteAmount
import com.kyberswap.android.domain.model.ResponseStatus
import com.kyberswap.android.domain.model.UserInfo
import javax.inject.Inject

class UserMapper @Inject constructor() {
    fun transform(entity: ResponseStatusEntity): ResponseStatus {
        return ResponseStatus(entity)
    }

    fun transform(entity: LoginUserEntity): LoginUser {
        return LoginUser(entity)
    }

    fun transform(entity: UserInfoEntity): UserInfo {
        return UserInfo(entity)
    }

    fun transform(entity: KycResponseStatusEntity): KycResponseStatus {
        return KycResponseStatus(entity)
    }

    fun transform(alerts: List<AlertEntity>): List<Alert> {
        return alerts.map {
            Alert(it)
        }
    }

    fun transform(entity: AlertMethodsResponseEntity): AlertMethodsResponse {
        return AlertMethodsResponse(entity)
    }

    fun transform(entity: QuoteAmountEntity): QuoteAmount {
        return QuoteAmount(entity)
    }
}