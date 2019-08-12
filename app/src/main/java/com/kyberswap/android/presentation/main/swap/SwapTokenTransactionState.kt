package com.kyberswap.android.presentation.main.swap

import com.kyberswap.android.domain.model.ResponseStatus

sealed class SwapTokenTransactionState {
    object Loading : SwapTokenTransactionState()
    class ShowError(val message: String?) : SwapTokenTransactionState()
    class Success(val responseStatus: ResponseStatus? = null) : SwapTokenTransactionState()
}
