package com.kyberswap.android.domain.usecase.alert

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Alert
import com.kyberswap.android.domain.repository.AlertRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import retrofit2.Response
import javax.inject.Inject

class DeleteAlertsUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val alertRepository: AlertRepository
) : SequentialUseCase<DeleteAlertsUseCase.Param, Response<Void>>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<Response<Void>> {
        return alertRepository.deleteAlert(param)
    }


    class Param(val alert: Alert)
}
