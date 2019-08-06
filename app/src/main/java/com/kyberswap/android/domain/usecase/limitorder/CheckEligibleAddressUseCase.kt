package com.kyberswap.android.domain.usecase.limitorder

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.EligibleAddress
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class CheckEligibleAddressUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val limitOrderRepository: LimitOrderRepository
) : SequentialUseCase<CheckEligibleAddressUseCase.Param, EligibleAddress>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<EligibleAddress> {
        return limitOrderRepository.eligibleAddress(param)
    }

    class Param(
        val wallet: Wallet
    )
}
