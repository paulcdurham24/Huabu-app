package com.huabu.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BadgeRarity { COMMON, RARE, EPIC, LEGENDARY }

@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val emoji: String,
    val description: String,
    val rarity: BadgeRarity = BadgeRarity.COMMON,
    val earnedAt: Long = System.currentTimeMillis(),
    val isVisible: Boolean = true
)

// Predefined system badges awarded automatically
object SystemBadges {
    val ALL = listOf(
        Badge("og_member",      "", "⭐", "OG Member",       "One of the first to join Huabu",         BadgeRarity.LEGENDARY),
        Badge("verified",       "", "✅", "Verified",         "Verified Huabu account",                 BadgeRarity.LEGENDARY),
        Badge("top_fan",        "", "🔥", "Top Fan",          "In the top fans list",                   BadgeRarity.EPIC),
        Badge("creator",        "", "🎨", "Creator",          "Shared original creative content",       BadgeRarity.EPIC),
        Badge("social_butterfly","","🦋", "Social Butterfly", "Has 100+ friends",                       BadgeRarity.RARE),
        Badge("music_lover",    "", "🎵", "Music Lover",      "Added a profile song",                   BadgeRarity.COMMON),
        Badge("event_host",     "", "📅", "Event Host",       "Created and hosted an event",            BadgeRarity.RARE),
        Badge("streamer",       "", "📡", "Streamer",         "Went live on Huabu",                     BadgeRarity.RARE),
        Badge("customiser",     "", "🎨", "Customiser",       "Customised their profile theme",         BadgeRarity.COMMON),
        Badge("popular",        "", "💫", "Popular",          "Post with 50+ likes",                    BadgeRarity.EPIC),
        Badge("night_owl",      "", "🦉", "Night Owl",        "Active after midnight",                  BadgeRarity.COMMON),
        Badge("trendsetter",    "", "👑", "Trendsetter",      "Trending on Huabu",                      BadgeRarity.LEGENDARY),
    )
}
