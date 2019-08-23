package com.kyberswap.android.presentation.main.limitorder

import com.kyberswap.android.domain.model.PendingBalances

sealed class GetPendingBalancesState {
    object Loading : GetPendingBalancesState()
    class ShowError(val message: String?) : GetPendingBalancesState()
    class Success(val pendingBalances: PendingBalances) : GetPendingBalancesState()
}
