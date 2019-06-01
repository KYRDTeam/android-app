package com.kyberswap.android.presentation.main.profile

import com.kyberswap.android.domain.model.LoginUser

sealed class LoginState {
    object Loading : LoginState()
    class ShowError(val message: String?) : LoginState()
    class Success(val login: LoginUser) : LoginState()
}
