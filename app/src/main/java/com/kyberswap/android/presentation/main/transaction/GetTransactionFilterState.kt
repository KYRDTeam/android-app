package com.kyberswap.android.presentation.main.transaction

import com.kyberswap.android.domain.model.FilterItem
import com.kyberswap.android.domain.model.TransactionFilter

sealed class GetTransactionFilterState {
    object Loading : GetTransactionFilterState()
    class ShowError(val message: String?) : GetTransactionFilterState()
    class Success(val transactionFilter: TransactionFilter, val tokens: List<FilterItem>) :
        GetTransactionFilterState()
}
