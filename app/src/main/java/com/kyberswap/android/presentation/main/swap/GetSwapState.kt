package com.kyberswap.android.presentation.main.swap

import com.kyberswap.android.domain.model.Swap

sealed class GetSwapState {
    object Loading : GetSwapState()
    class ShowError(val message: String?) : GetSwapState()
    class Success(val swap: Swap) : GetSwapState()
}
