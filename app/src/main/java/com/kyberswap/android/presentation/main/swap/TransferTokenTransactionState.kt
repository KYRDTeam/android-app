package com.kyberswap.android.presentation.main.swap

import com.kyberswap.android.domain.model.ResponseStatus

sealed class TransferTokenTransactionState {
    object Loading : TransferTokenTransactionState()
    class ShowError(val message: String?) : TransferTokenTransactionState()
    class Success(val responseStatus: ResponseStatus) : TransferTokenTransactionState()
}
