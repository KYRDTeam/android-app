package com.kyberswap.android.presentation.main.alert

import com.kyberswap.android.domain.model.Alert

sealed class GetAlertState {
    object Loading : GetAlertState()
    class ShowError(val message: String?) : GetAlertState()
    class Success(val alert: Alert) : GetAlertState()
}
