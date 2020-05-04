package com.kyberswap.android.presentation.main.balance

import com.kyberswap.android.domain.model.PendingBalances
import com.kyberswap.android.domain.model.Token

sealed class GetBalanceState {
    object Loading : GetBalanceState()
    class ShowError(val message: String?) : GetBalanceState()
    class Success(
        val tokens: List<Token>,
        val pendingBalances: PendingBalances? = null,
        val isCompleted: Boolean = true
    ) :
        GetBalanceState()
}
