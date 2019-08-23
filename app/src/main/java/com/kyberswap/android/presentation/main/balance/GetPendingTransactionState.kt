package com.kyberswap.android.presentation.main.balance

import com.kyberswap.android.domain.model.Transaction

sealed class GetPendingTransactionState {
    object Loading : GetPendingTransactionState()
    class ShowError(val message: String?) : GetPendingTransactionState()
    class Success(val transactions: List<Transaction>) : GetPendingTransactionState()
}
