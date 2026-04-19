package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.NftItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NftItemDao {
    @Query("SELECT * FROM nft_items WHERE userId = :userId ORDER BY acquiredAt DESC")
    fun getNftsForUser(userId: String): Flow<List<NftItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNft(nft: NftItem)

    @Query("DELETE FROM nft_items WHERE id = :nftId")
    suspend fun deleteNft(nftId: String)
}
