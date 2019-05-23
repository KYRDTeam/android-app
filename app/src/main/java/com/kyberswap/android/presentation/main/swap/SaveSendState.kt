package com.kyberswap.android.presentation.main.swap

sealed class SaveSendState {
    object Loading : SaveSendState()
    class ShowError(val message: String?) : SaveSendState()
    class Success(val message: String? = null) : SaveSendState()
}
