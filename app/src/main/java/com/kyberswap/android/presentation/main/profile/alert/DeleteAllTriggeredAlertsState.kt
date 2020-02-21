package com.kyberswap.android.presentation.main.profile.alert

import com.kyberswap.android.domain.model.ResponseStatus

sealed class DeleteAllTriggeredAlertsState {
    object Loading : DeleteAllTriggeredAlertsState()
    class ShowError(val message: String?) : DeleteAllTriggeredAlertsState()
    class Success(val responseStatus: ResponseStatus) : DeleteAllTriggeredAlertsState()
}
