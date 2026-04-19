package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String = "",
    val userId: String = "", // recipient
    val senderId: String = "",
    val senderName: String = "",
    val senderImageUrl: String = "",
    val type: String = "", // like, comment, follow, message, friend_request
    val title: String = "",
    val message: String = "",
    val read: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val targetId: String = "" // postId, userId, etc.
)
