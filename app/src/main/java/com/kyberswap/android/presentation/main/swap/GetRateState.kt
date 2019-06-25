package com.kyberswap.android.presentation.main.swap

sealed class GetRateState {
    object Loading : GetRateState()
    class ShowError(val message: String?) : GetRateState()
    class Success(val expectedRate: String, val marketRate: String) : GetRateState()
}
