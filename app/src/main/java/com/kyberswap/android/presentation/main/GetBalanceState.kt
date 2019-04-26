package com.kyberswap.android.presentation.main

import com.kyberswap.android.domain.model.token.Token

sealed class GetBalanceState {
    object Loading : GetBalanceState()
    class ShowError(val message: String?) : GetBalanceState()
    class Success(val token: Token) : GetBalanceState()
}
