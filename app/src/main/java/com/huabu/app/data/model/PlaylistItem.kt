package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlist_items")
data class PlaylistItem(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val artist: String,
    val albumArtColor: String = "#2D0045",
    val sortOrder: Int = 0
)
