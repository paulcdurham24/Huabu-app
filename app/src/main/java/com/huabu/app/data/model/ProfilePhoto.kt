package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PhotoFrameStyle {
    NONE,
    RAINBOW_GLOW,
    GOLD_ORNATE,
    NEON_PINK,
    COSMIC_PURPLE,
    RETRO_POLAROID,
    GLITTER_SILVER
}

@Entity(tableName = "profile_photos")
data class ProfilePhoto(
    @PrimaryKey val id: String,
    val userId: String,
    val imageUrl: String,
    val caption: String = "",
    val frameStyle: PhotoFrameStyle = PhotoFrameStyle.NONE,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
