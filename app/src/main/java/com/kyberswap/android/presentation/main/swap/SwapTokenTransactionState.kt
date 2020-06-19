package com.kyberswap.android.presentation.main.swap

import com.kyberswap.android.domain.model.ResponseStatus
import com.kyberswap.android.domain.model.Swap

sealed class SwapTokenTransactionState {
    object Loading : SwapTokenTransactionState()
    class ShowError(val message: String?, val swap: Swap? = null) : SwapTokenTransactionState()
    class Success(val responseStatus: ResponseStatus? = null, val swap: Swap? = null) :
        SwapTokenTransactionState()
}
