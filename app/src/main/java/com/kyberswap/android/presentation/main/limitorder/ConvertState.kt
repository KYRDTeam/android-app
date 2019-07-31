package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.domain.model.LocalLimitOrder

sealed class ConvertState {
    object Loading : ConvertState()
    class ShowError(val message: String?) : ConvertState()
    class Success(val limitOrder: LocalLimitOrder) : ConvertState()
}
