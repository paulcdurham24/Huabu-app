package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.MemeItem
import com.huabu.app.data.model.MemeReaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MemeItemDao {
    @Query("SELECT * FROM meme_items WHERE userId = :userId ORDER BY sortOrder ASC")
    fun getMemesForUser(userId: String): Flow<List<MemeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMeme(meme: MemeItem)

    @Query("DELETE FROM meme_items WHERE id = :memeId")
    suspend fun deleteMeme(memeId: String)

    @Query("UPDATE meme_items SET likes = likes + 1 WHERE id = :memeId")
    suspend fun likeMeme(memeId: String)

    @Query("UPDATE meme_items SET fire = fire + 1 WHERE id = :memeId")
    suspend fun fireMeme(memeId: String)

    @Query("UPDATE meme_items SET laugh = laugh + 1 WHERE id = :memeId")
    suspend fun laughMeme(memeId: String)

    @Query("UPDATE meme_items SET mindblown = mindblown + 1 WHERE id = :memeId")
    suspend fun mindblownMeme(memeId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordReaction(reaction: MemeReaction)

    @Query("SELECT reactionType FROM meme_reactions WHERE memeId = :memeId AND userId = :userId LIMIT 1")
    suspend fun getUserReaction(memeId: String, userId: String): String?
}
