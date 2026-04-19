package com.huabu.app.data.repository

import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.local.dao.NotificationDao
import com.huabu.app.data.model.Notification
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val firebaseService: FirebaseService
) {
    // Get notifications (real-time from Firestore)
    fun getNotifications(userId: String): Flow<List<Notification>> =
        firebaseService.getNotificationsFlow(userId)

    // Get unread count
    fun getUnreadCount(userId: String): Flow<Int> =
        notificationDao.getUnreadCount(userId)

    // Mark notification as read
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        // Update local
        notificationDao.markAsRead(notificationId)
        // Update remote
        return firebaseService.markNotificationRead(notificationId)
    }

    // Create notification
    suspend fun createNotification(notification: Notification): Result<Unit> {
        // Save locally
        notificationDao.insertNotification(notification)
        // Save to Firestore
        return firebaseService.createNotification(notification)
    }
}
