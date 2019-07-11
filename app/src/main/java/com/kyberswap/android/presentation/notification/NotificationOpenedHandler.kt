package com.kyberswap.android.presentation.notification

import android.content.Intent
import com.google.gson.Gson
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.domain.model.NotificationAlert
import com.kyberswap.android.presentation.main.MainActivity
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal


class NotificationOpenedHandler : OneSignal.NotificationOpenedHandler {

    // This fires when a notification is opened by tapping on it.
    override fun notificationOpened(result: OSNotificationOpenResult) {
        val data = result.notification.payload.additionalData
        try {
            val alert = Gson().fromJson(data.toString(), NotificationAlert::class.java)
            val intent = MainActivity.newIntent(KyberSwapApplication.instance, alert = alert)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            KyberSwapApplication.instance.startActivity(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }
}