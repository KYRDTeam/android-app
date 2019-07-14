package com.kyberswap.android.presentation.main.profile.alert

import com.kyberswap.android.domain.model.ResponseStatus

sealed class UpdateAlertMethodsState {
    object Loading : UpdateAlertMethodsState()
    class ShowError(val message: String?) : UpdateAlertMethodsState()
    class Success(val status: ResponseStatus) : UpdateAlertMethodsState()
}
