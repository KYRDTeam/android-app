package com.kyberswap.android.presentation.main.alert

import com.kyberswap.android.domain.model.Alert

sealed class CreateOrUpdateAlertState {
    object Loading : CreateOrUpdateAlertState()
    class ShowError(val message: String?) : CreateOrUpdateAlertState()
    class Success(val alert: Alert) : CreateOrUpdateAlertState()
}
