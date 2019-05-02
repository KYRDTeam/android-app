package com.kyberswap.android.presentation.main.balance

import com.kyberswap.android.domain.model.Token

sealed class GetBalanceState {
    object Loading : GetBalanceState()
    class ShowError(val message: String?) : GetBalanceState()
    class Success(val tokens: List<Token>) : GetBalanceState()
}
