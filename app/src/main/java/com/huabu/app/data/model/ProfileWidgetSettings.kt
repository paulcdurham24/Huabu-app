package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

const val DEFAULT_WIDGET_ORDER = "go_live,events,badges,pinned_posts,mood_board,nft_showcase,polls,code_snippets,tech_stack,gif_showcase,gif_showcase_1,gif_showcase_2,gif_showcase_3,spotify_now_playing,meme_wall,game_stats,visited_places,travel_wishlist,multiplayer_games,recently_played,my_playlist,currently_reading,currently_watching,photo_gallery,video_links,top_music,top_films,profile_song,about_me,interests,top_friends"

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
    val showBadges: Boolean = true,
    val showMoodBoard: Boolean = true,
    val showPinnedPosts: Boolean = true,
    val showRecentlyPlayed: Boolean = true,
    val showMyPlaylist: Boolean = true,
    val showCurrentlyReading: Boolean = true,
    val showCurrentlyWatching: Boolean = true,
    val showNftShowcase: Boolean = true,
    val showPolls: Boolean = true,
    val showCodeSnippets: Boolean = true,
    val showTechStack: Boolean = true,
    val showGifShowcase: Boolean = true,
    val showSpotifyNowPlaying: Boolean = true,
    val showMemeWall: Boolean = true,
    val showGameStats: Boolean = true,
    val showVisitedPlaces: Boolean = true,
    val showTravelWishlist: Boolean = true,
    val showMultiplayerGames: Boolean = true,
    val showMood: Boolean = true,
    val showLocation: Boolean = true,
    // 3 separate GIF showcase boxes for independent positioning
    val showGifShowcase1: Boolean = false,
    val showGifShowcase2: Boolean = false,
    val showGifShowcase3: Boolean = false,
    val widgetOrder: String = DEFAULT_WIDGET_ORDER,
    // Widget positioning and sizing (JSON strings)
    val widgetPositions: String = "", // {"widgetId": {"x": 0.0, "y": 0.0}, ...}
    val widgetSizes: String = "",     // {"widgetId": {"width": 1.0, "height": 0.3}, ...}
    val useGridLayout: Boolean = false, // false = freeform positioning, true = list layout
    val backgroundImageUrl: String = "" // Custom profile background image
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
        "badges"        -> showBadges
        "mood_board"    -> showMoodBoard
        "pinned_posts"       -> showPinnedPosts
        "recently_played"    -> showRecentlyPlayed
        "my_playlist"        -> showMyPlaylist
        "currently_reading"  -> showCurrentlyReading
        "currently_watching" -> showCurrentlyWatching
        "nft_showcase"       -> showNftShowcase
        "polls"              -> showPolls
        "code_snippets"      -> showCodeSnippets
        "tech_stack"         -> showTechStack
        "gif_showcase"       -> showGifShowcase
        "gif_showcase_1"     -> showGifShowcase1
        "gif_showcase_2"     -> showGifShowcase2
        "gif_showcase_3"     -> showGifShowcase3
        "spotify_now_playing"-> showSpotifyNowPlaying
        "meme_wall"          -> showMemeWall
        "game_stats"         -> showGameStats
        "visited_places"     -> showVisitedPlaces
        "travel_wishlist"    -> showTravelWishlist
        "multiplayer_games"  -> showMultiplayerGames
        else                 -> true
    }

    data class WidgetPosition(val x: Float, val y: Float)
    data class WidgetSize(val width: Float, val height: Float)

    fun getWidgetPosition(widgetId: String, index: Int): WidgetPosition {
        return try {
            val json = org.json.JSONObject(widgetPositions)
            val pos = json.optJSONObject(widgetId)
            if (pos != null) {
                WidgetPosition(
                    x = pos.optDouble("x", 0.0).toFloat(),
                    y = pos.optDouble("y", index * 0.15).toFloat()
                )
            } else {
                // Default grid position
                val row = index / 2
                val col = index % 2
                WidgetPosition(x = col * 0.5f, y = row * 0.2f)
            }
        } catch (e: Exception) {
            WidgetPosition(x = 0f, y = index * 0.15f)
        }
    }

    fun getWidgetSize(widgetId: String): WidgetSize {
        return try {
            val json = org.json.JSONObject(widgetSizes)
            val size = json.optJSONObject(widgetId)
            if (size != null) {
                WidgetSize(
                    width = size.optDouble("width", 1.0).toFloat(),
                    height = size.optDouble("height", 0.3).toFloat()
                )
            } else {
                WidgetSize(width = 1f, height = 0.3f)
            }
        } catch (e: Exception) {
            WidgetSize(width = 1f, height = 0.3f)
        }
    }
}
