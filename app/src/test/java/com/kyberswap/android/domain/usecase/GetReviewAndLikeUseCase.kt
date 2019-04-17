package com.kyberswap.android.domain.usecase

import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.HeaderInfo
import com.kyberswap.android.domain.repository.HeaderRepository
import io.reactivex.Flowable
import javax.inject.Inject

class GetReviewAndLikeUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val headersRepository: HeaderRepository
) : FlowableUseCase<String?, HeaderInfo>(schedulerProvider) {

    public override fun buildUseCaseFlowable(param: String?): Flowable<HeaderInfo> {
        return headersRepository.getLikeAndReviewInfo()
    }
}