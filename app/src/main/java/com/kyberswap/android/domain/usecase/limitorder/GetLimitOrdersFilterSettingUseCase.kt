package com.kyberswap.android.domain.usecase.limitorder

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.FilterItem
import com.kyberswap.android.domain.model.FilterSetting
import com.kyberswap.android.domain.model.OrderFilter.Companion.TOKEN_PAIR_SEPARATOR
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.MergeDelayErrorUseCase
import com.kyberswap.android.util.ext.shortenValue
import io.reactivex.Flowable
import io.reactivex.rxkotlin.Flowables
import javax.inject.Inject

class GetLimitOrdersFilterSettingUseCase @Inject constructor(
    schedulerProvider: SchedulerProvider,
    private val limitOrderRepository: LimitOrderRepository
) : MergeDelayErrorUseCase<String?, FilterSetting>(schedulerProvider) {
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    override fun buildUseCaseFlowable(param: String?): Flowable<FilterSetting> {

        return Flowables.zip(
            limitOrderRepository.getOrderFilter(),
            limitOrderRepository.getLimitOrders()
        ) { filter, orders ->

            val pairs = mutableSetOf<Pair<String, String>>()
            val address = mutableSetOf<String>()
            orders.forEach {
                pairs.add(it.src to it.dst)
                address.add(it.userAddr)
            }
            val unSelectedStatus = filter.unSelectedStatus
            val unSelectedAddress = filter.unSelectedAddresses
            val unSelectedPair = filter.unSelectedPairs
            val pairsSetting = pairs.map {
                FilterItem(
                    !unSelectedPair.contains(it), StringBuilder().append(it.first).append(
                        TOKEN_PAIR_SEPARATOR
                    ).append(it.second).toString()
                )
            }

            val addressSetting = address.map {
                FilterItem(!unSelectedAddress.contains(it), it, it.shortenValue())
            }

            val statusSettings = FilterSetting.DEFAULT_ORDER_STATUS.map {
                FilterItem(!unSelectedStatus.contains(it), it)
            }

            FilterSetting(pairsSetting, statusSettings, addressSetting, filter.oldest, filter)
        }
    }
}
