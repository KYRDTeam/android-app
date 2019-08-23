package com.kyberswap.android.presentation.main.limitorder

sealed class SaveLimitOrderState {
    object Loading : SaveLimitOrderState()
    class ShowError(val message: String?) : SaveLimitOrderState()
    class Success(val status: String?) : SaveLimitOrderState()
}
