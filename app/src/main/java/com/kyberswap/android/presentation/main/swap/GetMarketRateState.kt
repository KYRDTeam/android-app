package com.kyberswap.android.presentation.main.swap

sealed class GetMarketRateState {
    object Loading : GetMarketRateState()
    class ShowError(val message: String?, val isNetworkUnAvailable: Boolean = false) :
        GetMarketRateState()
    class Success(val rate: String) : GetMarketRateState()
}
