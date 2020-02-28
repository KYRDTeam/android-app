package com.kyberswap.android.presentation.main.balance.chart

import com.kyberswap.android.data.api.chart.Data

sealed class GetVol24hState {
    object Loading : GetVol24hState()
    class ShowError(val message: String?) : GetVol24hState()
    class Success(val data: Data) : GetVol24hState()
}
