package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.GifItem
import kotlinx.coroutines.flow.Flow

@Dao
interface GifItemDao {
    @Query("SELECT * FROM gif_items WHERE userId = :userId ORDER BY sortOrder ASC")
    fun getGifsForUser(userId: String): Flow<List<GifItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGif(gif: GifItem)

    @Query("DELETE FROM gif_items WHERE id = :gifId")
    suspend fun deleteGif(gifId: String)

    @Query("UPDATE gif_items SET repeat = :repeat WHERE id = :gifId")
    suspend fun updateRepeat(gifId: String, repeat: Boolean)
}
