package com.kyberswap.android.data.api.notification


import com.google.gson.annotations.SerializedName

data class SubscriptionNotificationEntity(
    @SerializedName("data")
    val `data`: List<SubscriptionNotificationDataEntity> = listOf(),
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String = ""
)