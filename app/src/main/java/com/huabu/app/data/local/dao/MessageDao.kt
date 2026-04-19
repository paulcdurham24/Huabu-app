package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE (senderId = :userId AND receiverId = :otherId) OR (senderId = :otherId AND receiverId = :userId) ORDER BY timestamp ASC")
    fun getConversation(userId: String, otherId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE receiverId = :userId AND isRead = 0")
    fun getUnreadMessages(userId: String): Flow<List<Message>>

    @Query("SELECT COUNT(*) FROM messages WHERE receiverId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)

    @Query("UPDATE messages SET isRead = 1 WHERE receiverId = :userId AND senderId = :senderId")
    suspend fun markConversationRead(userId: String, senderId: String)

    @Delete
    suspend fun deleteMessage(message: Message)
}
