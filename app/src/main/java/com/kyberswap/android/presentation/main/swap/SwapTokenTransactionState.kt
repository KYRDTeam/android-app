package com.kyberswap.android.presentation.main.swap

sealed class SwapTokenTransactionState {
    object Loading : SwapTokenTransactionState()
    class ShowError(val message: String?) : SwapTokenTransactionState()
    class Success(val transactionHash: String) : SwapTokenTransactionState()
}
