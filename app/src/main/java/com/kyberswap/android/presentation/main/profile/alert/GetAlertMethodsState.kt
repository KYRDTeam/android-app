package com.kyberswap.android.presentation.main.profile.alert

import com.kyberswap.android.domain.model.AlertMethods

sealed class GetAlertMethodsState {
    object Loading : GetAlertMethodsState()
    class ShowError(val message: String?) : GetAlertMethodsState()
    class Success(val alertMethods: AlertMethods) : GetAlertMethodsState()
}
