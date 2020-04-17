package com.kyberswap.android.domain.usecase.limitorder

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.MarketItem
import com.kyberswap.android.domain.model.ResponseStatus
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.SequentialUseCase
import io.reactivex.Single
import javax.inject.Inject

class SaveMarketItemUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val limitOrderRepository: LimitOrderRepository
) : SequentialUseCase<SaveMarketItemUseCase.Param, ResponseStatus>(schedulerProvider) {

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseSingle(param: Param): Single<ResponseStatus> {
        return limitOrderRepository.saveMarketIem(param)
    }

    class Param(val marketItem: MarketItem, val isLogin: Boolean)
}
