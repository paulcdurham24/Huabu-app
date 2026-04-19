package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.Notification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<Notification>>

    @Query("SELECT * FROM notifications WHERE userId = :userId AND read = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(userId: String): Flow<List<Notification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND read = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<Notification>)

    @Update
    suspend fun updateNotification(notification: Notification)

    @Query("UPDATE notifications SET read = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)

    @Query("UPDATE notifications SET read = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Delete
    suspend fun deleteNotification(notification: Notification)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
