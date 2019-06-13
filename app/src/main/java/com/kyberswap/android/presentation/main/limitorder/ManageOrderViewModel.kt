package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.data.repository.LimitOrderDataRepository.Companion.TOKEN_PAIR_SEPARATOR
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrderFilterUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrdersUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.util.ext.displayWalletAddress
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ManageOrderViewModel @Inject constructor(
    private val getLimitOrderFilterUseCase: GetLimitOrderFilterUseCase,
    private val getLimitOrdersUseCase: GetLimitOrdersUseCase

) : ViewModel() {

    private val _getRelatedOrderCallback = MutableLiveData<Event<GetRelatedOrdersState>>()
    val getRelatedOrderCallback: LiveData<Event<GetRelatedOrdersState>>
        get() = _getRelatedOrderCallback

    private val _getFilterStateCallback = MutableLiveData<Event<GetFilterState>>()
    val getFilterStateCallback: LiveData<Event<GetFilterState>>
        get() = _getFilterStateCallback

    fun getRelatedOrders(wallet: Wallet, orderFilter: OrderFilter) {
        getLimitOrdersUseCase.execute(
            Consumer {

                val orderByOldest = orderFilter.oldest
                val addresses = orderFilter.listAddress.filter {
                    it.isSelected
                }.map {
                    it.name
                }
                val status = orderFilter.listStatus.filter {
                    it.isSelected
                }.map {
                    it.name
                }

                val pairs = orderFilter.listOrders.filter {
                    it.isSelected
                }.map {
                    it.name
                }

                val orders = if (orderByOldest) {
                    it.sortedBy { it.createdAt }
                } else {
                    it.sortedByDescending {
                        it.createdAt
                    }
                }.filter {
                    if (addresses.isNotEmpty()) {
                        addresses.contains(it.userAddr.displayWalletAddress())
                    } else {
                        true
                    }
                }.filter {
                    if (status.isNotEmpty()) {
                        status.contains(it.status)
                    } else {
                        true
                    }
                }.filter {
                    if (pairs.isNotEmpty()) {
                        pairs.contains(
                            StringBuilder().append(it.src).append(TOKEN_PAIR_SEPARATOR).append(it.dst)
                                .toString()
                        )
                    } else {
                        true
                    }
                }

                _getRelatedOrderCallback.value =
                    Event(GetRelatedOrdersState.Success(orders))
            },
            Consumer {
                it.printStackTrace()
                _getRelatedOrderCallback.value =
                    Event(GetRelatedOrdersState.ShowError(it.localizedMessage))

            },
            GetLimitOrdersUseCase.Param(wallet.address)
        )
    }

    fun getFilter(wallet: Wallet) {
        getLimitOrderFilterUseCase.execute(
            Consumer {
                getRelatedOrders(wallet, it)
                _getFilterStateCallback.value = Event(GetFilterState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getFilterStateCallback.value =
                    Event(GetFilterState.ShowError(it.localizedMessage))
            },
            GetLimitOrderFilterUseCase.Param(wallet.address)
        )
    }
}