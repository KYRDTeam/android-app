package com.kyberswap.android.presentation.setting

sealed class SavePinState {
    object Loading : SavePinState()
    class ShowError(val message: String?) : SavePinState()
    class Success(val message: String? = null) : SavePinState()
}
