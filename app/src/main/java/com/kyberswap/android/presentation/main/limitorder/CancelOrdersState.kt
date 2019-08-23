package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.domain.model.Cancelled

sealed class CancelOrdersState {
    object Loading : CancelOrdersState()
    class ShowError(val message: String?) : CancelOrdersState()
    class Success(val cancelled: Cancelled) : CancelOrdersState()
}
