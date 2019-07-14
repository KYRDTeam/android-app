package com.kyberswap.android.domain.usecase.setting

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.repository.SettingRepository
import com.kyberswap.android.domain.usecase.CompletableUseCase
import io.reactivex.Completable
import javax.inject.Inject

class SavePinUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val settingRepository: SettingRepository
) : CompletableUseCase<SavePinUseCase.Param, Any?>(schedulerProvider) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseCompletable(param: Param): Completable {
        return settingRepository.savePin(param)
    }

    class Param(val pin: String)
}
