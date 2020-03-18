package com.kyberswap.android.presentation.main.notification

import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.domain.model.SubscriptionNotificationData

sealed class TogglePriceNotificationState {
    object Loading : TogglePriceNotificationState()
    class ShowError(val message: String?) : TogglePriceNotificationState()
    class Success(val message: String? = "") : TogglePriceNotificationState()
}
