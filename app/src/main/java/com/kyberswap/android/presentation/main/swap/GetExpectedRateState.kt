package com.kyberswap.android.presentation.main.swap

sealed class GetExpectedRateState {
    object Loading : GetExpectedRateState()
    class ShowError(val message: String?) : GetExpectedRateState()
    class Success(val list: List<String>) : GetExpectedRateState()
}
