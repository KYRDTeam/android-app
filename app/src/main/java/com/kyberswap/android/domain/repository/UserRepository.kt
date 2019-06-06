package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.LoginUser
import com.kyberswap.android.domain.model.UserStatus
import com.kyberswap.android.domain.usecase.profile.LoginSocialUseCase
import com.kyberswap.android.domain.usecase.profile.LoginUseCase
import com.kyberswap.android.domain.usecase.profile.ResetPasswordUseCase
import com.kyberswap.android.domain.usecase.profile.SignUpUseCase
import io.reactivex.Single

interface UserRepository {
    fun signUp(param: SignUpUseCase.Param): Single<UserStatus>

    fun login(param: LoginUseCase.Param): Single<LoginUser>

    fun loginSocial(param: LoginSocialUseCase.Param): Single<LoginUser>

    fun resetPassword(param: ResetPasswordUseCase.Param): Single<UserStatus>
}
