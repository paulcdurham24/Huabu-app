package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_tracks")
data class RecentTrack(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val artist: String,
    val albumArtColor: String = "#1A1A2E",
    val playedAt: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 180
)
