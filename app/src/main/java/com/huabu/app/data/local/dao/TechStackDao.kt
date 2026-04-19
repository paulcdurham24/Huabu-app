package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.TechStackItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TechStackDao {
    @Query("SELECT * FROM tech_stack_items WHERE userId = :userId ORDER BY sortOrder ASC")
    fun getTechStackForUser(userId: String): Flow<List<TechStackItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: TechStackItem)

    @Query("DELETE FROM tech_stack_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: String)
}
