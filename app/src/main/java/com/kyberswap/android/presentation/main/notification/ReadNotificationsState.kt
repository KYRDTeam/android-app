package com.kyberswap.android.presentation.main.notification

import com.kyberswap.android.domain.model.Notification

sealed class ReadNotificationsState {
    object Loading : ReadNotificationsState()
    class ShowError(val message: String?) : ReadNotificationsState()
    class Success(val success: Boolean, val notification: Notification? = null) :
        ReadNotificationsState()
}