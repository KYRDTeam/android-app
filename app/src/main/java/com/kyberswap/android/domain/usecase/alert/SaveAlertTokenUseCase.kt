package com.kyberswap.android.domain.usecase.alert

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Token
import com.kyberswap.android.domain.repository.AlertRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class SaveAlertTokenUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val alertRepository: AlertRepository
) : CompletableUseCase<SaveAlertTokenUseCase.Param, Any?>(schedulerProvider) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Param): Completable {
        return alertRepository.saveAlertToken(param)
    }

    class Param(val walletAddress: String, val token: Token)
}
