package com.kyberswap.android.presentation.main.profile

import com.kyberswap.android.domain.model.ResponseStatus

sealed class ResetPasswordState {
    object Loading : ResetPasswordState()
    class ShowError(val message: String?) : ResetPasswordState()
    class Success(val status: ResponseStatus) : ResetPasswordState()
}
