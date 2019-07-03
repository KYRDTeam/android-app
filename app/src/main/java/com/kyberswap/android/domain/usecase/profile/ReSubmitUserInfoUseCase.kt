package com.kyberswap.android.domain.usecase.profile

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.KycResponseStatus
import com.kyberswap.android.domain.model.UserInfo
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class ReSubmitUserInfoUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val userRepository: UserRepository
) : SequentialUseCase<ReSubmitUserInfoUseCase.Param, KycResponseStatus>(schedulerProvider) {


    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<KycResponseStatus> {
        return userRepository.reSubmit(param)
    }


    class Param(val user: UserInfo)
}
