package com.kyberswap.android.domain.model

import com.google.gson.annotations.SerializedName
import com.kyberswap.android.data.api.notification.SubscriptionNotificationDataEntity

data class SubscriptionNotificationData(
    @SerializedName("symbol")
    val symbol: String = "",
    @SerializedName("subscribed")
    var subscribed: Boolean = false
) {
    constructor(entity: SubscriptionNotificationDataEntity) : this(entity.symbol, entity.subscribed)
}