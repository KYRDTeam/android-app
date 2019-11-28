package com.kyberswap.android.presentation.main.walletconnect

sealed class RequestState {
    object Loading : RequestState()
    class ShowError(val message: String?) : RequestState()
    class Success(val status: Boolean = false) : RequestState()
}
