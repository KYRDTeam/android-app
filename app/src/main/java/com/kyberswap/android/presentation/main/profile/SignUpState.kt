package com.kyberswap.android.presentation.main.profile

import com.kyberswap.android.domain.model.UserStatus

sealed class SignUpState {
    object Loading : SignUpState()
    class ShowError(val message: String?) : SignUpState()
    class Success(val registerStatus: UserStatus) : SignUpState()
}
