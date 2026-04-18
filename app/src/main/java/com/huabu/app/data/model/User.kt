package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val bio: String = "",
    val profileImageUrl: String = "",
    val backgroundImageUrl: String = "",
    val profileSong: String = "",
    val profileSongArtist: String = "",
    val location: String = "",
    val website: String = "",
    val mood: String = "",
    val interests: String = "",
    val aboutMe: String = "",
    val heroesSection: String = "",
    val profileColor1: String = "#6A0572",
    val profileColor2: String = "#0077FF",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val joinedDate: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)
