package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_widget_settings")
data class ProfileWidgetSettings(
    @PrimaryKey val userId: String,
    val showPhotoGallery: Boolean = true,
    val showVideoLinks: Boolean = true,
    val showTopMusic: Boolean = true,
    val showTopFilms: Boolean = true,
    val showProfileSong: Boolean = true,
    val showAboutMe: Boolean = true,
    val showInterests: Boolean = true,
    val showTopFriends: Boolean = true,
    val showMood: Boolean = true,
    val showLocation: Boolean = true
)
