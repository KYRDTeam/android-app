package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.domain.model.Order

sealed class GetRelatedOrdersState {
    object Loading : GetRelatedOrdersState()
    class ShowError(val message: String?) : GetRelatedOrdersState()
    class Success(val orders: List<Order>) : GetRelatedOrdersState()
}
