package com.example.notifications

import com.example.data.model.HKPayConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class HKPayMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        runCatching {
            val messaging = FirebaseMessaging.getInstance()
            messaging.subscribeToTopic("all_users")
            messaging.subscribeToTopic("user_$uid")
            messaging.subscribeToTopic("orders_$uid")
            messaging.subscribeToTopic("order_status_updates")
        }
        FirebaseDatabase.getInstance(HKPayConstants.FIREBASE_DATABASE_URL)
            .reference
            .child("users")
            .child(uid)
            .child("fcmToken")
            .setValue(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val orderId = data["orderId"] ?: data["referenceId"].orEmpty()
        val orderStatus = data["orderStatus"] ?: data["status"] ?: data["newStatus"].orEmpty()
        val previousStatus = data["previousStatus"].orEmpty()
        val customerName = data["customerName"]
            ?: FirebaseAuth.getInstance().currentUser?.displayName.orEmpty()
        val cardName = data["cardName"] ?: data["packageName"].orEmpty()
        val trackingNumber = data["trackingNumber"].orEmpty()
        val type = data["type"] ?: if (orderId.isNotBlank()) "Order Status" else "System Notice"

        val (personalizedTitle, personalizedBody) = if (orderId.isNotBlank() && orderStatus.isNotBlank()) {
            NotificationHelper.buildPersonalizedOrderMessage(
                customerName = customerName,
                packageName = cardName,
                orderId = orderId,
                orderStatus = orderStatus,
                trackingNumber = trackingNumber,
                previousStatus = previousStatus
            )
        } else {
            val t = message.notification?.title ?: data["title"] ?: "HK PAY Update"
            val b = message.notification?.body ?: data["message"] ?: data["body"] ?: return
            t to b
        }

        val finalTitle = message.notification?.title ?: data["title"] ?: personalizedTitle
        val finalBody = message.notification?.body ?: data["message"] ?: personalizedBody

        NotificationHelper.showNotification(
            context = applicationContext,
            title = finalTitle,
            body = finalBody,
            type = type,
            orderId = orderId
        )

        // Sync incoming personalized push into the signed-in user's notification center
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null && data["skipDbSync"] != "true") {
            val dbRef = FirebaseDatabase.getInstance(HKPayConstants.FIREBASE_DATABASE_URL)
                .reference
                .child("notifications")
                .child(uid)
            val key = data["notificationId"]?.takeIf { it.isNotBlank() } ?: dbRef.push().key
            if (!key.isNullOrBlank()) {
                dbRef.child(key).setValue(
                    mapOf(
                        "id" to key,
                        "userId" to uid,
                        "title" to finalTitle,
                        "message" to finalBody,
                        "type" to type,
                        "referenceId" to orderId,
                        "isRead" to false,
                        "createdAt" to System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
