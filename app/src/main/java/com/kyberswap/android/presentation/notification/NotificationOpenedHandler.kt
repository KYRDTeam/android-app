package com.kyberswap.android.presentation.notification

import android.content.Intent
import com.google.gson.Gson
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.domain.model.NotificationAlert
import com.kyberswap.android.domain.model.NotificationLimitOrder
import com.kyberswap.android.presentation.main.MainActivity
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal


class NotificationOpenedHandler : OneSignal.NotificationOpenedHandler {

    // This fires when a notification is opened by tapping on it.
    override fun notificationOpened(result: OSNotificationOpenResult) {
        val data = result.notification.payload.additionalData

        try {
            val type = data.getString(NOTIFICATION_TYPE)
            if (type == NOTIFICATION_TYPE_ALERT) {
                val alert = Gson().fromJson(data.toString(), NotificationAlert::class.java)
                val intent = MainActivity.newIntent(KyberSwapApplication.instance, alert = alert)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                KyberSwapApplication.instance.startActivity(intent)
     else if (type == NOTIFICATION_TYPE_LIMITORDER) {
                val limitOrder =
                    Gson().fromJson(data.toString(), NotificationLimitOrder::class.java)
                val intent = MainActivity.newIntent(
                    KyberSwapApplication.instance,
                    limitOrderNotification = limitOrder
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                KyberSwapApplication.instance.startActivity(intent)
    

 catch (ex: Exception) {
            ex.printStackTrace()


    }

    companion object {
        const val NOTIFICATION_TYPE = "type"
        const val NOTIFICATION_TYPE_ALERT = "alert_price"
        const val NOTIFICATION_TYPE_LIMITORDER = "limit_order"
    }
}