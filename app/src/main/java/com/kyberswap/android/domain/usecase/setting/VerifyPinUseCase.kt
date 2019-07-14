package com.kyberswap.android.domain.usecase.setting

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.VerifyStatus
import com.kyberswap.android.domain.repository.SettingRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class VerifyPinUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val settingRepository: SettingRepository
) : SequentialUseCase<VerifyPinUseCase.Param, VerifyStatus>(schedulerProvider) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<VerifyStatus> {
        return settingRepository.verifyPin(param)
    }

    class Param(val pin: String, val remainNum: Int, val time: Long)
}
