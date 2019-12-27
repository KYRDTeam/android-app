package com.kyberswap.android.presentation.main.notification

import com.kyberswap.android.domain.model.Notification

sealed class GetNotificationsState {
    object Loading : GetNotificationsState()
    class ShowError(val message: String?) : GetNotificationsState()
    class Success(val notifications: List<Notification>) : GetNotificationsState()
}
