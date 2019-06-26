package com.kyberswap.android.presentation.main.alert

import com.kyberswap.android.domain.model.Alert

sealed class GetCurrentAlertState {
    object Loading : GetCurrentAlertState()
    class ShowError(val message: String?) : GetCurrentAlertState()
    class Success(val alert: Alert) : GetCurrentAlertState()
}
