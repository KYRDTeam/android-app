package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.model.OrderFilter
import com.kyberswap.android.domain.usecase.limitorder.CancelOrderUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrderFilterUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrdersUseCase
import com.kyberswap.android.presentation.common.Event
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


    private fun getAllOrders(orderFilter: OrderFilter) {
        getLimitOrdersUseCase.execute(
            Consumer {
                orderList = it

                _getRelatedOrderCallback.value =
                    Event(
                        GetRelatedOrdersState.Success(
                            toOrderItems(
                                filterOrders(it, orderFilter),
                                orderFilter.oldest
                            )
                        )
                    )
            },
            Consumer {
                it.printStackTrace()
                _getRelatedOrderCallback.value =
                    Event(GetRelatedOrdersState.ShowError(it.localizedMessage))

            },
            null
        )
    }

    private fun filterOrders(
        orders: List<Order>,
        orderFilter: OrderFilter
    ): List<Order> {
        return orders
            .filter {
                orderFilter.status.map { it.toLowerCase() }.contains(it.status.toLowerCase()) &&
                    orderFilter.pairs[it.src] == it.dst &&
                    orderFilter.addresses.contains(it.userAddr)
            }

    }

    private fun toOrderItems(orders: List<Order>, asc: Boolean): List<OrderItem> {
        return if (asc) {
            orders.sortedBy { it.time }
        } else {
            orders.sortedByDescending { it.time }
        }.groupBy { it.shortedDateTimeFormat }
            .flatMap { item ->
                val items = mutableListOf<OrderItem>()
                items.add(OrderItem.Header(item.key))
                val list =
                    if (asc) {
                        item.value.sortedBy { it.time }
                    } else {
                        item.value.sortedByDescending { it.time }
                    }

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

    fun getFilter() {
        getLimitOrderFilterUseCase.dispose()
        getLimitOrderFilterUseCase.execute(
            Consumer {
                if (orderFilter != it) {
                    orderFilter = it
                    getAllOrders(it)
                }
                _getFilterCallback.value = Event(GetFilterState.Success(it))
            },
            Consumer {
                it.printStackTrace()
                _getFilterCallback.value =
                    Event(GetFilterState.ShowError(it.localizedMessage))
            },
            null
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