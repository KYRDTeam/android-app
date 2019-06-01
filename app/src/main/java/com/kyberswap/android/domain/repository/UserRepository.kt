package com.kyberswap.android.domain.repository

import com.kyberswap.android.domain.model.LoginUser
import com.kyberswap.android.domain.model.RegisterStatus
import com.kyberswap.android.domain.usecase.wallet.LoginUseCase
import com.kyberswap.android.domain.usecase.wallet.SignUpUseCase
import io.reactivex.Single

interface UserRepository {
    fun signUp(param: SignUpUseCase.Param): Single<RegisterStatus>

    fun login(param: LoginUseCase.Param): Single<LoginUser>
}
