package com.kyberswap.android.domain.usecase.profile

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.ResponseStatus
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class UpdatePushTokenUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val userRepository: UserRepository
) : SequentialUseCase<UpdatePushTokenUseCase.Param, ResponseStatus>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    public override fun buildUseCaseSingle(param: Param): Single<ResponseStatus> {
        return userRepository.updatePushNotification(param)
    }

    class Param(
        val userId: String,
        val token: String
    )
}
