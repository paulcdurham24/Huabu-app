package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.PlaylistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistItemDao {
    @Query("SELECT * FROM playlist_items WHERE userId = :userId ORDER BY sortOrder ASC")
    fun getPlaylist(userId: String): Flow<List<PlaylistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: PlaylistItem)

    @Query("DELETE FROM playlist_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: String)
}
