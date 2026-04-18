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
        ProfileWidgetSettings::class
    ],
    version = 3,
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
}
