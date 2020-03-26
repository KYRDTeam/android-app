package com.kyberswap.android.presentation.main.notification

import com.kyberswap.android.domain.model.Notification
import com.kyberswap.android.domain.model.SubscriptionNotificationData

sealed class UpdateSubscribedTokensNotificationState {
    object Loading : UpdateSubscribedTokensNotificationState()
    class ShowError(val message: String?) : UpdateSubscribedTokensNotificationState()
    class Success(val message: String? = "") : UpdateSubscribedTokensNotificationState()
}
