package com.kyberswap.android.presentation.main.swap

sealed class GetExpectedRateState {
    object Loading : GetExpectedRateState()
    class ShowError(val message: String?, val isNetworkUnAvailable: Boolean = false) :
        GetExpectedRateState()
    class Success(val list: List<String>) : GetExpectedRateState()
}
