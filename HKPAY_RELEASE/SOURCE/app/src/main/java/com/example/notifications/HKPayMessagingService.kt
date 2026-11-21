package com.example.notifications

import com.example.data.model.HKPayConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class HKPayMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance(HKPayConstants.FIREBASE_DATABASE_URL)
            .reference
            .child("users")
            .child(uid)
            .child("fcmToken")
            .setValue(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "HK PAY Update"
        val body = message.notification?.body ?: message.data["message"] ?: return
        val type = message.data["type"] ?: "Order Created"

        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            body = body,
            type = type
        )
    }
}
