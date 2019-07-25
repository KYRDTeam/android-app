package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.data.repository.LimitOrderDataRepository
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.domain.usecase.limitorder.CancelOrderUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrderFilterUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrdersUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.util.ext.displayWalletAddress
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ManageOrderViewModel @Inject constructor(
    private val getLimitOrderFilterUseCase: GetLimitOrderFilterUseCase,
    private val getLimitOrdersUseCase: GetLimitOrdersUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase

) : ViewModel() {

    private val _getRelatedOrderCallback = MutableLiveData<Event<GetRelatedOrdersState>>()
    val getRelatedOrderCallback: LiveData<Event<GetRelatedOrdersState>>
        get() = _getRelatedOrderCallback

    private val _cancelOrderCallback = MutableLiveData<Event<CancelOrdersState>>()
    val cancelOrderCallback: LiveData<Event<CancelOrdersState>>
        get() = _cancelOrderCallback

    private val _getFilterCallback = MutableLiveData<Event<GetFilterState>>()
    val getFilterCallback: LiveData<Event<GetFilterState>>
        get() = _getFilterCallback

    private var orderList: List<Order> = listOf()

    private var orderFilter: OrderFilter? = null


    fun getRelatedOrders(wallet: Wallet) {
        getLimitOrdersUseCase.execute(
            Consumer {
                orderList = it
                getFilter(wallet)
                _getRelatedOrderCallback.value =
                    Event(GetRelatedOrdersState.Success(toOrderItems(it)))
            },
            Consumer {
                it.printStackTrace()
                _getRelatedOrderCallback.value =
                    Event(GetRelatedOrdersState.ShowError(it.localizedMessage))

            },
            GetLimitOrdersUseCase.Param(wallet.address)
        )
    }

    fun getCurrentFilterList(): List<OrderItem> {
        return orderFilter?.let {
            filterOrders(it)
        } ?: listOf()

    }

    fun filterOrders(filter: OrderFilter): List<OrderItem> {
        orderFilter = filter
        val orderByOldest = filter.oldest
        val addresses = filter.listAddress.filter {
            it.isSelected
        }.map {
            it.name
        }

        val pairs = filter.listOrders.filter {
            it.isSelected
        }.map {
            it.name
        }

        return toOrderItems(if (orderByOldest) {
            orderList.sortedBy { it.createdAt }
        } else {
            orderList.sortedByDescending {
                it.createdAt
            }
        }.filter {
            if (addresses.isNotEmpty()) {
                addresses.contains(it.userAddr.displayWalletAddress())
            } else {
                true
            }
        }.filter {
            if (filter.status.isNotEmpty()) {
                filter.status.contains(it.status)
            } else {
                true
            }
        }.filter {
            if (pairs.isNotEmpty()) {
                pairs.contains(
                    StringBuilder().append(it.src).append(LimitOrderDataRepository.TOKEN_PAIR_SEPARATOR).append(
                        it.dst
                    )
                        .toString()
                )
            } else {
                true
            }
        })

    }

    private fun toOrderItems(orders: List<Order>): List<OrderItem> {
        return orders.groupBy { it.shortedDateTimeFormat }
            .flatMap { item ->
                val items = mutableListOf<OrderItem>()
                items.add(OrderItem.Header(item.key))
                val list = item.value.sortedByDescending { it.time }
                list.forEachIndexed { index, transaction ->
                    if (index % 2 == 0) {
                        items.add(OrderItem.ItemEven(transaction))
                    } else {
                        items.add(OrderItem.ItemOdd(transaction))
                    }
                }
                items
            }
    }

    private fun getFilter(wallet: Wallet) {
        getLimitOrderFilterUseCase.execute(
            Consumer {
                _getFilterCallback.value = Event(GetFilterState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getFilterCallback.value =
                    Event(GetFilterState.ShowError(it.localizedMessage))
            },
            GetLimitOrderFilterUseCase.Param(wallet.address)
        )
    }

    fun cancelOrder(order: Order) {
        _cancelOrderCallback.postValue(Event(CancelOrdersState.Loading))
        cancelOrderUseCase.execute(
            Consumer {
                _cancelOrderCallback.value = Event(CancelOrdersState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _cancelOrderCallback.value =
                    Event(CancelOrdersState.ShowError(it.localizedMessage))
            },
            CancelOrderUseCase.Param(order)
        )
    }
}