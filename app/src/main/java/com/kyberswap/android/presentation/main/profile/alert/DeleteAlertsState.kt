package com.kyberswap.android.presentation.main.profile.alert

import com.kyberswap.android.domain.model.ResponseStatus

sealed class DeleteAlertsState {
    object Loading : DeleteAlertsState()
    class ShowError(val message: String?) : DeleteAlertsState()
    class Success(val status: ResponseStatus) : DeleteAlertsState()
}
