package com.kyberswap.android.domain.usecase.limitorder

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.SelectedMarketItem
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.FlowableUseCase
import io.reactivex.Flowable
import javax.inject.Inject

class GetSelectedMarketUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val limitOrderRepository: LimitOrderRepository
) : FlowableUseCase<GetSelectedMarketUseCase.Param, SelectedMarketItem>(schedulerProvider) {
//    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
//    override fun buildUseCaseFlowable(param: Param): Single<SelectedMarketItem> {
//        return limitOrderRepository.getSelectedMarket(param)
//    }

    class Param(val wallet: Wallet)

//    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
//    override fun buildUseCaseSingle(param: Param): Single<SelectedMarketItem> {
//        return limitOrderRepository.getSelectedMarket(param)
//    }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: Param): Flowable<SelectedMarketItem> {
        return limitOrderRepository.getSelectedMarket(param)
    }
}
