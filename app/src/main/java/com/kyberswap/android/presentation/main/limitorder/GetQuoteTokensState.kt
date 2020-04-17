package com.kyberswap.android.presentation.main.limitorder

sealed class GetQuoteTokensState {
    object Loading : GetQuoteTokensState()
    class ShowError(val message: String?) :
        GetQuoteTokensState()

    class Success(val quotes: List<String>) : GetQuoteTokensState()
}
