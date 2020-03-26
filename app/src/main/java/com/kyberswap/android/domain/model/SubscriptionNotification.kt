package com.kyberswap.android.domain.model

import com.google.gson.annotations.SerializedName
import com.kyberswap.android.data.api.notification.SubscriptionNotificationEntity

data class SubscriptionNotification(
    @SerializedName("data")
    val `data`: List<SubscriptionNotificationData> = listOf(),
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("price_noti")
    val priceNoti: Boolean = false
) {
    constructor(entity: SubscriptionNotificationEntity) : this(
        entity.data.map {
            SubscriptionNotificationData(it)
        },
        entity.success,
        entity.message,
        entity.priceNoti
    )
}