package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "live_streams")
data class LiveStream(
    @PrimaryKey val userId: String,
    val isLive: Boolean = false,
    val title: String = "",
    val viewerCount: Int = 0,
    val startedAt: Long = 0L,
    val thumbnailUrl: String = ""
)
