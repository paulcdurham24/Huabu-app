package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spotify_tracks")
data class SpotifyTrack(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtUrl: String = "",
    val durationMs: Int = 0,
    val isPlaying: Boolean = false,
    val progressMs: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
