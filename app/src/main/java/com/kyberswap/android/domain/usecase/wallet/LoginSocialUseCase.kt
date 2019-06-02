package com.kyberswap.android.domain.usecase.wallet

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.LoginUser
import com.kyberswap.android.domain.model.SocialInfo
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class LoginSocialUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val userRepository: UserRepository
) : SequentialUseCase<LoginSocialUseCase.Param, LoginUser>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun buildUseCaseSingle(param: Param): Single<LoginUser> {
        return userRepository.loginSocial(param)
    }

    class Param(
        val socialInfo: SocialInfo
    )
}
