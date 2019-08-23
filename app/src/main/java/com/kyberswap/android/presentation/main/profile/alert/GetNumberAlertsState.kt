package com.kyberswap.android.presentation.main.profile.alert

sealed class GetNumberAlertsState {
    object Loading : GetNumberAlertsState()
    class ShowError(val message: String?) : GetNumberAlertsState()
    class Success(val numOfAlert: Int) : GetNumberAlertsState()
}
