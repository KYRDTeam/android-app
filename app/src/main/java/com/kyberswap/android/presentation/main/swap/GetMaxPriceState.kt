package com.kyberswap.android.presentation.main.swap

sealed class GetMaxPriceState {
    object Loading : GetMaxPriceState()
    class ShowError(val message: String?) : GetMaxPriceState()
    class Success(val data: String) : GetMaxPriceState()
}
