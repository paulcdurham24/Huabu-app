package com.huabu.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.huabu.app.data.local.dao.*
import com.huabu.app.data.model.*

@Database(
    entities = [
        User::class,
        Post::class,
        Message::class,
        Friend::class,
        ProfilePhoto::class,
        VideoLink::class,
        MediaTrack::class,
        ProfileWidgetSettings::class,
        ProfileTheme::class,
        LiveStream::class,
        ProfileEvent::class,
        Badge::class,
        MoodBoardItem::class,
        PinnedPost::class,
        RecentTrack::class,
        PlaylistItem::class,
        CurrentlyReading::class,
        CurrentlyWatching::class,
        NftItem::class,
        ProfilePoll::class,
        PollVote::class,
        CodeSnippet::class,
        TechStackItem::class,
        GifItem::class,
        SpotifyTrack::class,
        MemeItem::class,
        MemeReaction::class,
        GameStats::class,
        DailyScore::class,
        VisitedPlace::class,
        TravelWish::class
    ],
    version = 10,
    exportSchema = false
)
abstract class HuabuDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun messageDao(): MessageDao
    abstract fun friendDao(): FriendDao
    abstract fun profilePhotoDao(): ProfilePhotoDao
    abstract fun videoLinkDao(): VideoLinkDao
    abstract fun mediaTrackDao(): MediaTrackDao
    abstract fun profileWidgetSettingsDao(): ProfileWidgetSettingsDao
    abstract fun profileThemeDao(): ProfileThemeDao
    abstract fun liveStreamDao(): LiveStreamDao
    abstract fun profileEventDao(): ProfileEventDao
    abstract fun badgeDao(): BadgeDao
    abstract fun moodBoardDao(): MoodBoardDao
    abstract fun pinnedPostDao(): PinnedPostDao
    abstract fun recentTrackDao(): RecentTrackDao
    abstract fun playlistItemDao(): PlaylistItemDao
    abstract fun currentlyReadingDao(): CurrentlyReadingDao
    abstract fun currentlyWatchingDao(): CurrentlyWatchingDao
    abstract fun nftItemDao(): NftItemDao
    abstract fun profilePollDao(): ProfilePollDao
    abstract fun codeSnippetDao(): CodeSnippetDao
    abstract fun techStackDao(): TechStackDao
    abstract fun gifItemDao(): GifItemDao
    abstract fun spotifyTrackDao(): SpotifyTrackDao
    abstract fun memeItemDao(): MemeItemDao
    abstract fun gameStatsDao(): GameStatsDao
    abstract fun visitedPlaceDao(): VisitedPlaceDao
    abstract fun travelWishDao(): TravelWishDao
}
