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

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val ordersChannel = NotificationChannel(
                context.getString(R.string.fcm_channel_orders_id),
                context.getString(R.string.fcm_channel_orders_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Card order, shipment, delivery, and activation updates"
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
                description = "System notices and currency rate updates"
            }

            manager.createNotificationChannels(listOf(ordersChannel, securityChannel, generalChannel))
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        type: String
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
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
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

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}
