package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    const val EXTRA_ORDER_ID = "extra_hkpay_order_id"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val ordersChannel = NotificationChannel(
                context.getString(R.string.fcm_channel_orders_id),
                context.getString(R.string.fcm_channel_orders_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Personalized card order, shipment, delivery, and activation updates"
            }

            val securityChannel = NotificationChannel(
                context.getString(R.string.fcm_channel_security_id),
                context.getString(R.string.fcm_channel_security_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Security alerts and KYC verification status updates"
            }

            val generalChannel = NotificationChannel(
                context.getString(R.string.fcm_channel_general_id),
                context.getString(R.string.fcm_channel_general_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "System notices, referrals, and currency rate updates"
            }

            manager.createNotificationChannels(listOf(ordersChannel, securityChannel, generalChannel))
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        type: String,
        orderId: String = ""
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val channelId = when {
            type.contains("Security", ignoreCase = true) || type.contains("KYC", ignoreCase = true) ->
                context.getString(R.string.fcm_channel_security_id)
            type.contains("System", ignoreCase = true) ->
                context.getString(R.string.fcm_channel_general_id)
            else -> context.getString(R.string.fcm_channel_orders_id)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (orderId.isNotBlank()) {
                putExtra(EXTRA_ORDER_ID, orderId)
            }
        }
        val notifId = if (orderId.isNotBlank()) orderId.hashCode() else System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    fun buildPersonalizedOrderMessage(
        customerName: String,
        packageName: String,
        orderId: String,
        orderStatus: String,
        trackingNumber: String = "",
        previousStatus: String = ""
    ): Pair<String, String> {
        val firstName = customerName.trim().substringBefore(" ").ifBlank { "Member" }
        val cardLabel = packageName.ifBlank { "HK PAY Physical Card" }
        val normalizedStatus = orderStatus.trim()
        val title = when (normalizedStatus) {
            "Payment Confirmed" -> "$firstName, Payment Confirmed!"
            "KYC Approved" -> "$firstName, KYC Approved"
            "Processing", "Card Processing" -> "$cardLabel is Being Prepared"
            "Card Issued" -> "Your $cardLabel is Issued!"
            "Shipped", "In Transit" -> "On the Way: $cardLabel"
            "Delivered" -> "$cardLabel Delivered!"
            "Active" -> "$cardLabel is Now Active"
            else -> "Order Update: $normalizedStatus"
        }
        val body = buildString {
            if (previousStatus.isNotBlank() && !previousStatus.equals(normalizedStatus, ignoreCase = true)) {
                append("Hi $firstName, your $cardLabel ($orderId) moved from $previousStatus to $normalizedStatus.")
            } else {
                append("Hi $firstName, your $cardLabel ($orderId) status updated to $normalizedStatus.")
            }
            if (trackingNumber.isNotBlank()) {
                append(" Tracking: $trackingNumber.")
            }
        }
        return title to body
    }
}
