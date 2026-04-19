package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.GameInvite
import com.huabu.app.data.model.InviteStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GameInviteDao {
    @Query("SELECT * FROM game_invites WHERE toUserId = :userId AND status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingInvitesForUser(userId: String): Flow<List<GameInvite>>

    @Query("SELECT * FROM game_invites WHERE fromUserId = :userId ORDER BY createdAt DESC")
    fun getSentInvites(userId: String): Flow<List<GameInvite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun sendInvite(invite: GameInvite)

    @Query("UPDATE game_invites SET status = :status, respondedAt = :now WHERE id = :inviteId")
    suspend fun respondToInvite(inviteId: String, status: InviteStatus, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM game_invites WHERE id = :inviteId")
    suspend fun deleteInvite(inviteId: String)
}
