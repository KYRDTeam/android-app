package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.domain.model.Order

sealed class SubmitOrderState {
    object Loading : SubmitOrderState()
    class ShowError(val message: String?) : SubmitOrderState()
    class Success(val order: Order) : SubmitOrderState()
}
