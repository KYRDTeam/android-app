package com.kyberswap.android.presentation.main.notification

import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.domain.model.SubscriptionNotificationData

sealed class GetSubscriptionNotificationState {
    object Loading : GetSubscriptionNotificationState()
    class ShowError(val message: String?) : GetSubscriptionNotificationState()
    class Success(val notifications: List<SubscriptionNotificationData>, val priceNoti: Boolean) : GetSubscriptionNotificationState()
}
