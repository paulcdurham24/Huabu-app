package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE participantIds LIKE '%' || :userId || '%' ORDER BY lastMessageTimestamp DESC")
    fun getConversationsForUser(userId: String): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    fun getConversationById(conversationId: String): Flow<Conversation?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<Conversation>)

    @Update
    suspend fun updateConversation(conversation: Conversation)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("UPDATE conversations SET unreadCount = :count WHERE id = :conversationId")
    suspend fun updateUnreadCount(conversationId: String, count: Int)
}
