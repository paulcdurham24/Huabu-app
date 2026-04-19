package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class InviteStatus { PENDING, ACCEPTED, DECLINED, EXPIRED }
enum class GameType2P { TICTACTOE, MINESWEEPER }

@Entity(tableName = "game_invites")
data class GameInvite(
    @PrimaryKey val id: String,
    val gameType: GameType2P,
    val gameId: String, // ID of the created game
    val fromUserId: String,
    val fromUserName: String,
    val toUserId: String,
    val toUserName: String,
    val message: String = "",
    val status: InviteStatus = InviteStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val respondedAt: Long = 0
)
