package com.kyberswap.android.domain.usecase.alert

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.ResponseStatus
import com.kyberswap.android.domain.repository.AlertRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class DeleteAllAlertsUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val alertRepository: AlertRepository
) : SequentialUseCase<DeleteAllAlertsUseCase.Param, ResponseStatus>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<ResponseStatus> {
        return alertRepository.deleteAllTriggerAlerts(param)
    }


    class Param
}
