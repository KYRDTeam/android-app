package com.kyberswap.android.data.api.notification


import com.google.gson.annotations.SerializedName

data class SubscriptionNotificationDataEntity(
    @SerializedName("symbol")
    val symbol: String = "",
    @SerializedName("subscribed")
    val subscribed: Boolean = false
)