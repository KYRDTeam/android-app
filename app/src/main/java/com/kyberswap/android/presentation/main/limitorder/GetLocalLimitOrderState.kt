package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.domain.model.LocalLimitOrder

sealed class GetLocalLimitOrderState {
    object Loading : GetLocalLimitOrderState()
    class ShowError(val message: String?) : GetLocalLimitOrderState()
    class Success(val order: LocalLimitOrder) : GetLocalLimitOrderState()
}
