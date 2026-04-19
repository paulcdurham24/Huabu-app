package com.huabu.app.data.repository

import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.local.dao.ConversationDao
import com.huabu.app.data.local.dao.MessageDao
import com.huabu.app.data.model.Conversation
import com.huabu.app.data.model.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val firebaseService: FirebaseService
) {
    // Get messages for a conversation (real-time from Firestore)
    fun getMessages(conversationId: String): Flow<List<Message>> =
        firebaseService.getMessagesFlow(conversationId)

    // Get user's conversations (real-time from Firestore)
    fun getConversations(userId: String): Flow<List<Conversation>> =
        firebaseService.getConversationsFlow(userId)

    // Send a message
    suspend fun sendMessage(conversationId: String, message: Message): Result<Unit> {
        return firebaseService.sendMessage(conversationId, message)
    }

    // Create or get conversation
    suspend fun getOrCreateConversation(
        userId1: String,
        userId2: String
    ): String {
        // Generate consistent conversation ID
        val conversationId = if (userId1 < userId2) {
            "${userId1}_${userId2}"
        } else {
            "${userId2}_${userId1}"
        }
        return conversationId
    }
}
