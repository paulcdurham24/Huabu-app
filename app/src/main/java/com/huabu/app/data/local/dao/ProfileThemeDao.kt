package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.ProfileTheme
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileThemeDao {
    @Query("SELECT * FROM profile_themes WHERE userId = :userId")
    fun getThemeForUser(userId: String): Flow<ProfileTheme?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTheme(theme: ProfileTheme)
}
