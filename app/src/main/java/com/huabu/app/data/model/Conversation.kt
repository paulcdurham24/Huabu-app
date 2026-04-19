package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val lastMessageSenderId: String = "",
    val unreadCount: Int = 0
)

// UI model for conversation with user details
data class ConversationUI(
    val conversationId: String,
    val otherUser: User,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val unreadCount: Int = 0
)
