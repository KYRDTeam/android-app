package com.kyberswap.android.presentation.main.transaction

import com.kyberswap.android.domain.model.TransactionFilter

sealed class GetTransactionState {
    object Loading : GetTransactionState()
    class ShowError(val message: String?) : GetTransactionState()
    class Success(
        val transactions: List<TransactionItem>,
        val transactionFilter: TransactionFilter
    ) : GetTransactionState()
}
