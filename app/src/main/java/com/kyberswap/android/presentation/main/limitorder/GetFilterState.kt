package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.domain.model.OrderFilter

sealed class GetFilterState {
    object Loading : GetFilterState()
    class ShowError(val message: String?) : GetFilterState()
    class Success(val orderFilter: OrderFilter) : GetFilterState()
}
