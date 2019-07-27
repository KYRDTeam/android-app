package com.kyberswap.android.presentation.main.limitorder

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kyberswap.android.domain.model.Order
import com.kyberswap.android.domain.usecase.limitorder.CancelOrderUseCase
import com.kyberswap.android.domain.usecase.limitorder.GetLimitOrdersUseCase
import com.kyberswap.android.domain.usecase.profile.GetLoginStatusUseCase
import com.kyberswap.android.presentation.common.Event
import com.kyberswap.android.presentation.main.GetLoginStatusViewModel
import io.reactivex.functions.Consumer
import javax.inject.Inject

class ManageOrderViewModel @Inject constructor(
    private val getLimitOrdersUseCase: GetLimitOrdersUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    val getLoginStatusUseCase: GetLoginStatusUseCase

) : GetLoginStatusViewModel(getLoginStatusUseCase) {

    private val _getRelatedOrderCallback = MutableLiveData<Event<GetRelatedOrdersState>>()
    val getRelatedOrderCallback: LiveData<Event<GetRelatedOrdersState>>
        get() = _getRelatedOrderCallback

    private val _cancelOrderCallback = MutableLiveData<Event<CancelOrdersState>>()
    val cancelOrderCallback: LiveData<Event<CancelOrdersState>>
        get() = _cancelOrderCallback

    var ordersWrapper: OrdersWrapper? = null

    override fun onCleared() {
        getLimitOrdersUseCase.dispose()
        cancelOrderUseCase.dispose()
        super.onCleared()
    }


    fun getAllOrders() {
        getLimitOrdersUseCase.execute(
            Consumer {
                ordersWrapper = it
                _getRelatedOrderCallback.value =
                    Event(
                        GetRelatedOrdersState.Success(toOrderItems(it.orders, it.asc))
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

    fun toOrderItems(orders: List<Order>, asc: Boolean): List<OrderItem> {
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