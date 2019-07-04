package com.kyberswap.android.domain.usecase.profile

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import com.kyberswap.android.presentation.main.profile.kyc.KycInfoType
import io.reactivex.Completable
import javax.inject.Inject

class SaveKycInfoUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val userRepository: UserRepository
) : CompletableUseCase<SaveKycInfoUseCase.Param, Any?>(schedulerProvider) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Param): Completable {
        return userRepository.save(param)
    }

    class Param(val value: String, val kycInfoType: KycInfoType)
}
