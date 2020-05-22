package com.kyberswap.android.presentation.setting

import com.freshchat.consumer.sdk.Freshchat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FreshchatMesssagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        Freshchat.getInstance(this).setPushRegistrationToken(token!!)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (Freshchat.isFreshchatNotification(remoteMessage)) {
            Freshchat.handleFcmMessage(this, remoteMessage)
        }
    }
}