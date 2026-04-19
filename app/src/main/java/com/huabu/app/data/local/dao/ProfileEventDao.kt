package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.ProfileEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileEventDao {
    @Query("SELECT * FROM profile_events WHERE userId = :userId ORDER BY eventDate ASC")
    fun getEventsForUser(userId: String): Flow<List<ProfileEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvent(event: ProfileEvent)

    @Delete
    suspend fun deleteEvent(event: ProfileEvent)

    @Query("DELETE FROM profile_events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: String)
}
