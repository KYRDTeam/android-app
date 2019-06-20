package com.kyberswap.android.presentation.main.profile

sealed class LogoutState {
    object Loading : LogoutState()
    class ShowError(val message: String?) : LogoutState()
    class Success(val status: String?) : LogoutState()
}
