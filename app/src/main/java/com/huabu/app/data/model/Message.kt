package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val senderName: String,
    val senderImageUrl: String = "",
    val content: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class Conversation(
    val conversationId: String,
    val otherUser: User,
    val lastMessage: Message?,
    val unreadCount: Int = 0
)
