package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey val id: String,
    val userId: String,
    val friendId: String,
    val friendName: String,
    val friendUsername: String,
    val friendImageUrl: String = "",
    val isTopFriend: Boolean = false,
    val topFriendRank: Int = -1,
    val addedAt: Long = System.currentTimeMillis(),
    val status: String = "accepted" // pending, accepted, rejected
)
