package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.Friend
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends WHERE userId = :userId ORDER BY isTopFriend DESC, topFriendRank ASC")
    fun getFriends(userId: String): Flow<List<Friend>>

    @Query("SELECT * FROM friends WHERE userId = :userId")
    fun getAllFriends(userId: String): Flow<List<Friend>>

    @Query("SELECT * FROM friends WHERE userId = :userId AND isTopFriend = 1 ORDER BY topFriendRank ASC LIMIT 8")
    fun getTopFriends(userId: String): Flow<List<Friend>>

    @Query("SELECT COUNT(*) FROM friends WHERE userId = :userId")
    fun getFriendCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: Friend)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<Friend>)

    @Query("DELETE FROM friends WHERE userId = :userId AND friendId = :friendId")
    suspend fun removeFriend(userId: String, friendId: String)

    @Update
    suspend fun updateFriend(friend: Friend)
}
