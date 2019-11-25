package com.kyberswap.android.domain.usecase.profile

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.KycResponseStatus
import com.kyberswap.android.domain.model.RatingInfo
import com.kyberswap.android.domain.repository.UserRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class SaveRatingInfoUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val userRepository: UserRepository
) : CompletableUseCase<SaveRatingInfoUseCase.Param, KycResponseStatus>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Param): Completable {
        return userRepository.saveRating(param)
    }

    class Param(val ratingInfo: RatingInfo)
}
