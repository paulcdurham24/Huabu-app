package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_links")
data class VideoLink(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val url: String,
    val thumbnailUrl: String = "",
    val description: String = "",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
