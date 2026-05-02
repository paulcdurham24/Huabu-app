package com.huabu.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.huabu.app.MainActivity
import com.huabu.app.R
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.FirebaseService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HuabuMessagingService : FirebaseMessagingService() {

    @Inject lateinit var firebaseService: FirebaseService
    @Inject lateinit var authService: AuthService

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = authService.getCurrentUserId() ?: return
        CoroutineScope(Dispatchers.IO).launch {
            firebaseService.saveFcmToken(userId, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["message"] ?: ""
        val type = message.data["type"] ?: ""
        val targetId = message.data["targetId"] ?: ""
        showNotification(title, body, type, targetId)
    }

    private fun showNotification(title: String, body: String, type: String = "", targetId: String = "") {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val msgChannelId = "huabu_messages"
        val socialChannelId = "huabu_social"

        NotificationChannel(msgChannelId, "Messages", NotificationManager.IMPORTANCE_HIGH)
            .apply { description = "Direct messages"; enableLights(true); enableVibration(true) }
            .also { manager.createNotificationChannel(it) }

        NotificationChannel(socialChannelId, "Social", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Likes, comments, friend requests" }
            .also { manager.createNotificationChannel(it) }

        val channelId = if (type == "message") msgChannelId else socialChannelId

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_type", type)
            putExtra("notification_target_id", targetId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (type == "message") NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
