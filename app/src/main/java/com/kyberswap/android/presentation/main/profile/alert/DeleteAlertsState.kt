package com.kyberswap.android.presentation.main.profile.alert

sealed class DeleteAlertsState {
    object Loading : DeleteAlertsState()
    class ShowError(val message: String?) : DeleteAlertsState()
    class Success(val status: String? = "") : DeleteAlertsState()
}
