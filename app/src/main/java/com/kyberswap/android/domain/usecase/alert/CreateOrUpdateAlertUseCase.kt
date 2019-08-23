package com.kyberswap.android.domain.usecase.alert

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.repository.AlertRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class CreateOrUpdateAlertUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val alertRepository: AlertRepository
) : SequentialUseCase<CreateOrUpdateAlertUseCase.Param, Alert>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<Alert> {
        return alertRepository.createOrUpdateAlert(param)
    }


    class Param(val alert: Alert)
}
