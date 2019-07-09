package com.kyberswap.android.domain.usecase.setting

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.repository.SettingRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class GetPinUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val settingRepository: SettingRepository
) : SequentialUseCase<String?, String?>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: String?): Single<String?> {
        return settingRepository.getPin()
    }
}
