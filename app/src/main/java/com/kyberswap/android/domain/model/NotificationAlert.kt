package com.kyberswap.android.domain.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotificationAlert(
    @SerializedName("type")
    val type: String = "",
    @SerializedName("alert_id")
    val alertId: Long = 0L,
    @SerializedName("test_alert_id")
    val testAlertId: String = "",
    @SerializedName("base")
    val base: String = "",
    @SerializedName("token")
    val token: String = "",
    @SerializedName("notification_id")
    val notificationId: Long = 0
) : Parcelable {

    constructor(notificationExt: NotificationExt) : this(
        base = notificationExt.base,
        token = if (notificationExt.base.isEmpty()) Token.ETH_SYMBOL else notificationExt.token,
        alertId = notificationExt.alertId
    )

    val pair: String
        get() = StringBuilder()
            .append(token)
            .append("/")
            .append(base).toString()

    companion object {
        const val ALERT_PRICE = "alert_price"
    }
}