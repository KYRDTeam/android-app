package com.kyberswap.android.presentation.main.limitorder

sealed class GetNonceState {
    object Loading : GetNonceState()
    class ShowError(val message: String?, val isNetworkUnAvailable: Boolean = false) :
        GetNonceState()
    class Success(val nonce: String) : GetNonceState()
}
