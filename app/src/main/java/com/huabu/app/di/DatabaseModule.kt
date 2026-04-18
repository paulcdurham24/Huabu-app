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
}
