package com.kyberswap.android.presentation.main.limitorder

sealed class GetRelatedOrdersState {
    object Loading : GetRelatedOrdersState()
    class ShowError(val message: String?, val isNetworkUnAvailable: Boolean = false) :
        GetRelatedOrdersState()
    class Success(val orders: List<OrderItem>) : GetRelatedOrdersState()
}
