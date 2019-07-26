package com.kyberswap.android.domain.usecase.limitorder

import androidx.annotation.VisibleForTesting
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.FilterItem
import com.kyberswap.android.domain.model.FilterSetting
import com.kyberswap.android.domain.model.OrderFilter.Companion.TOKEN_PAIR_SEPARATOR
import com.kyberswap.android.domain.repository.LimitOrderRepository
import com.kyberswap.android.domain.usecase.MergeDelayErrorUseCase
import com.kyberswap.android.util.ext.displayWalletAddress
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

            val pairs = mutableMapOf<String, String>()
            val address = mutableSetOf<String>()
            orders.forEach {
                pairs[it.src] = it.dst
                address.add(it.userAddr)
            }
            val selectedStatus = filter.status
            val selectedAddress = filter.addresses
            val selectedPair = filter.pairs
            val pairsSetting = pairs.map {
                FilterItem(
                    selectedPair[it.key] == it.value, StringBuilder().append(it.key).append(
                        TOKEN_PAIR_SEPARATOR
                    ).append(it.value).toString()
                )
            }

            val addressSetting = address.map {
                FilterItem(selectedAddress.contains(it), it, it.displayWalletAddress())
            }

            val statusSettings = FilterSetting.DEFAULT_ORDER_STATUS.map {
                FilterItem(selectedStatus.contains(it), it)
            }

            FilterSetting(pairsSetting, statusSettings, addressSetting, filter.oldest, filter)
        }
    }
}
