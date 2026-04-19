package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

const val DEFAULT_WIDGET_ORDER = "go_live,events,photo_gallery,video_links,top_music,top_films,profile_song,about_me,interests,top_friends"

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
    val showGoLive: Boolean = true,
    val showEvents: Boolean = true,
    val showMood: Boolean = true,
    val showLocation: Boolean = true,
    val widgetOrder: String = DEFAULT_WIDGET_ORDER
) {
    fun orderedWidgetIds(): List<String> =
        widgetOrder.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun isEnabled(widgetId: String): Boolean = when (widgetId) {
        "photo_gallery" -> showPhotoGallery
        "video_links"   -> showVideoLinks
        "top_music"     -> showTopMusic
        "top_films"     -> showTopFilms
        "profile_song"  -> showProfileSong
        "about_me"      -> showAboutMe
        "interests"     -> showInterests
        "top_friends"   -> showTopFriends
        "go_live"       -> showGoLive
        "events"        -> showEvents
        else            -> true
    }
}
