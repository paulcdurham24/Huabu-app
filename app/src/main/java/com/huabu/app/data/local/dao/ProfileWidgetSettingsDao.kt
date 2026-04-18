package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.ProfileWidgetSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileWidgetSettingsDao {
    @Query("SELECT * FROM profile_widget_settings WHERE userId = :userId")
    fun getSettingsForUser(userId: String): Flow<ProfileWidgetSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: ProfileWidgetSettings)
}
