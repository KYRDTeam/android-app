package com.kyberswap.android.domain.usecase

import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Header
import com.kyberswap.android.domain.repository.HeaderRepository
import io.reactivex.Flowable
import javax.inject.Inject

class GetHeadersUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val headersRepository: HeaderRepository
) : MergeDelayErrorUseCase<String?, List<Header>>(schedulerProvider) {

    public override fun buildUseCaseFlowable(param: String?): Flowable<List<Header>> {
        return headersRepository.headers()
    }
}