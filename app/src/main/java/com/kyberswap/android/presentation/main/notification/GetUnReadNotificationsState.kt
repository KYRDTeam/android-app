package com.kyberswap.android.presentation.main.notification

sealed class GetUnReadNotificationsState {
    object Loading : GetUnReadNotificationsState()
    class ShowError(val message: String?) : GetUnReadNotificationsState()
    class Success(val notifications: Int) : GetUnReadNotificationsState()
}
