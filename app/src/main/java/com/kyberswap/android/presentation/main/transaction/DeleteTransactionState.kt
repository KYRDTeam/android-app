package com.kyberswap.android.presentation.main.transaction

sealed class DeleteTransactionState {
    object Loading : DeleteTransactionState()
    class ShowError(val message: String?) : DeleteTransactionState()
    class Success(val status: String?) : DeleteTransactionState()
}
