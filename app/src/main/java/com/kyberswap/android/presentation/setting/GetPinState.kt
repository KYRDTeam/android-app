package com.kyberswap.android.presentation.setting

sealed class GetPinState {
    object Loading : GetPinState()
    class ShowError(val message: String?) : GetPinState()
    class Success(val digest: String?) : GetPinState()
}
