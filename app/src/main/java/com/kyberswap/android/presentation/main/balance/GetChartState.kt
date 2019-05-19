package com.kyberswap.android.presentation.main.balance

import com.kyberswap.android.domain.model.Chart

sealed class GetChartState {
    object Loading : GetChartState()
    class ShowError(val message: String?) : GetChartState()
    class Success(val chart: Chart) : GetChartState()
}
