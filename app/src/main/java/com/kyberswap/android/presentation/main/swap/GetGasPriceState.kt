package com.kyberswap.android.presentation.main.swap

import com.kyberswap.android.domain.model.Gas

sealed class GetGasPriceState {
    object Loading : GetGasPriceState()
    class ShowError(val message: String?) : GetGasPriceState()
    class Success(val gas: Gas) : GetGasPriceState()
}
