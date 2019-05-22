package com.kyberswap.android.presentation.main.swap

sealed class TransferTokenTransactionState {
    object Loading : TransferTokenTransactionState()
    class ShowError(val message: String?) : TransferTokenTransactionState()
    class Success(val transactionHash: String) : TransferTokenTransactionState()
}
