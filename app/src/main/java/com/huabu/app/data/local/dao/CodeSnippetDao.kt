package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.CodeSnippet
import kotlinx.coroutines.flow.Flow

@Dao
interface CodeSnippetDao {
    @Query("SELECT * FROM code_snippets WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSnippetsForUser(userId: String): Flow<List<CodeSnippet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: CodeSnippet)

    @Query("DELETE FROM code_snippets WHERE id = :snippetId")
    suspend fun deleteSnippet(snippetId: String)
}
