package com.kyberswap.android.presentation.main.swap

sealed class SaveSwapState {
    object Loading : SaveSwapState()
    class ShowError(val message: String?) : SaveSwapState()
    class Success(val isExpectedRateZero: Boolean) : SaveSwapState()
}
