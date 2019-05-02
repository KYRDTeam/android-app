package com.kyberswap.android.domain.usecase.wallet

import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.repository.WalletRepository
import com.kyberswap.android.domain.usecase.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class GetUnitUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val walletRepository: WalletRepository
) : FlowableUseCase<String?, String>(schedulerProvider) {
    override fun buildUseCaseFlowable(param: String?): Flowable<String> {
        return walletRepository.getSelectedUnit()
    }
}
