package com.huabu.app.di

import android.content.Context
import androidx.room.Room
import com.huabu.app.data.local.HuabuDatabase
import com.huabu.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHuabuDatabase(@ApplicationContext context: Context): HuabuDatabase {
        return Room.databaseBuilder(
            context,
            HuabuDatabase::class.java,
            "huabu_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(db: HuabuDatabase): UserDao = db.userDao()

    @Provides
    fun providePostDao(db: HuabuDatabase): PostDao = db.postDao()

    @Provides
    fun provideMessageDao(db: HuabuDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideFriendDao(db: HuabuDatabase): FriendDao = db.friendDao()

    @Provides
    fun provideProfilePhotoDao(db: HuabuDatabase): ProfilePhotoDao = db.profilePhotoDao()

    @Provides
    fun provideVideoLinkDao(db: HuabuDatabase): VideoLinkDao = db.videoLinkDao()

    @Provides
    fun provideMediaTrackDao(db: HuabuDatabase): MediaTrackDao = db.mediaTrackDao()

    @Provides
    fun provideProfileWidgetSettingsDao(db: HuabuDatabase): ProfileWidgetSettingsDao = db.profileWidgetSettingsDao()

    @Provides
    fun provideProfileThemeDao(db: HuabuDatabase): ProfileThemeDao = db.profileThemeDao()

    @Provides
    fun provideLiveStreamDao(db: HuabuDatabase): LiveStreamDao = db.liveStreamDao()

    @Provides
    fun provideProfileEventDao(db: HuabuDatabase): ProfileEventDao = db.profileEventDao()

    @Provides
    fun provideBadgeDao(db: HuabuDatabase): BadgeDao = db.badgeDao()

    @Provides
    fun provideMoodBoardDao(db: HuabuDatabase): MoodBoardDao = db.moodBoardDao()

    @Provides
    fun providePinnedPostDao(db: HuabuDatabase): PinnedPostDao = db.pinnedPostDao()

    @Provides
    fun provideRecentTrackDao(db: HuabuDatabase): RecentTrackDao = db.recentTrackDao()

    @Provides
    fun providePlaylistItemDao(db: HuabuDatabase): PlaylistItemDao = db.playlistItemDao()

    @Provides
    fun provideCurrentlyReadingDao(db: HuabuDatabase): CurrentlyReadingDao = db.currentlyReadingDao()

    @Provides
    fun provideCurrentlyWatchingDao(db: HuabuDatabase): CurrentlyWatchingDao = db.currentlyWatchingDao()

    @Provides
    fun provideNftItemDao(db: HuabuDatabase): NftItemDao = db.nftItemDao()

    @Provides
    fun provideProfilePollDao(db: HuabuDatabase): ProfilePollDao = db.profilePollDao()
}
