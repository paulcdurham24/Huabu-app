package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.MoodBoardItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodBoardDao {
    @Query("SELECT * FROM mood_board_items WHERE userId = :userId ORDER BY gridPosition ASC")
    fun getMoodBoardForUser(userId: String): Flow<List<MoodBoardItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: MoodBoardItem)

    @Query("DELETE FROM mood_board_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: String)

    @Query("DELETE FROM mood_board_items WHERE userId = :userId AND gridPosition = :position")
    suspend fun clearPosition(userId: String, position: Int)
}
