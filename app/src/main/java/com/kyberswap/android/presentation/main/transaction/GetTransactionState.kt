package com.kyberswap.android.presentation.main.transaction

sealed class GetTransactionState {
    object Loading : GetTransactionState()
    class ShowError(val message: String?) : GetTransactionState()
    class Success(
        val transactions: List<TransactionItem>,
        val isFilterChanged: Boolean,
        val isLoaded: Boolean
    ) : GetTransactionState()

    class FilterNotChange(val isLoaded: Boolean) : GetTransactionState()
}
