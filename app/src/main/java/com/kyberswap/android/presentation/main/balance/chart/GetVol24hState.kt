package com.kyberswap.android.presentation.main.balance.chart

sealed class GetVol24hState {
    object Loading : GetVol24hState()
    class ShowError(val message: String?) : GetVol24hState()
    class Success(val message: String) : GetVol24hState()
}
