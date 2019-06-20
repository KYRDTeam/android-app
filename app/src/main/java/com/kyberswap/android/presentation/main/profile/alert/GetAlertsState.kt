package com.kyberswap.android.presentation.main.profile.alert

import com.kyberswap.android.domain.model.Alert

sealed class GetAlertsState {
    object Loading : GetAlertsState()
    class ShowError(val message: String?) : GetAlertsState()
    class Success(val alerts: List<Alert>) : GetAlertsState()
}
