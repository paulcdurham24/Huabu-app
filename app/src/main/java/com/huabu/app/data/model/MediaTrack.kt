package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MediaTrackType { MUSIC, FILM }

@Entity(tableName = "media_tracks")
data class MediaTrack(
    @PrimaryKey val id: String,
    val userId: String,
    val type: MediaTrackType,
    val title: String,
    val subtitle: String = "",
    val artworkUrl: String = "",
    val year: String = "",
    val rank: Int = 1
)
