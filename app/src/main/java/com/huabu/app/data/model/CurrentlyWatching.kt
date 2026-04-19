package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WatchType { SERIES, FILM, ANIME, DOCUMENTARY }

@Entity(tableName = "currently_watching")
data class CurrentlyWatching(
    @PrimaryKey val userId: String,
    val title: String = "",
    val type: WatchType = WatchType.SERIES,
    val coverColor: String = "#0D2137",
    val season: Int = 1,
    val episode: Int = 1,
    val totalEpisodes: Int = 0,
    val progressPercent: Int = 0,   // 0-100
    val rating: Float = 0f,         // 0-5
    val platform: String = ""       // Netflix, HBO etc.
)
