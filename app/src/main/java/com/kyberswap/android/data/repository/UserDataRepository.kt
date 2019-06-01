package com.kyberswap.android.data.repository

import com.kyberswap.android.data.api.home.UserApi
import com.kyberswap.android.data.mapper.UserMapper
import com.kyberswap.android.domain.model.LoginUser
import com.kyberswap.android.domain.model.RegisterStatus
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.usecase.wallet.LoginUseCase
import com.kyberswap.android.domain.usecase.wallet.SignUpUseCase
import io.reactivex.Single
import javax.inject.Inject


class UserDataRepository @Inject constructor(
    private val userApi: UserApi,
    private val userMapper: UserMapper
) : UserRepository {
    override fun login(param: LoginUseCase.Param): Single<LoginUser> {
        return userApi.login(param.email, param.password)
            .map { userMapper.transform(it) }
    }

    override fun signUp(param: SignUpUseCase.Param): Single<RegisterStatus> {
        return userApi.register(
            param.email,
            param.password,
            param.password,
            param.displayName,
            param.isSubscription
        ).map {
            userMapper.transform(it)
        }
    }


}