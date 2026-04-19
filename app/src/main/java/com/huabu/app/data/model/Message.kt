package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val conversationId: String = "",
    val senderId: String,
    val receiverId: String,
    val senderName: String,
    val senderImageUrl: String = "",
    val content: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
