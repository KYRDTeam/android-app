package com.kyberswap.android.presentation.main.swap

sealed class SaveSwapDataState {
    object Loading : SaveSwapDataState()
    class ShowError(val message: String?) : SaveSwapDataState()
    class Success(val message: String? = null) : SaveSwapDataState()
}
