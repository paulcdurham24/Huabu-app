package com.huabu.app.data.local.dao

import androidx.room.*
import com.huabu.app.data.model.ProfilePhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfilePhotoDao {
    @Query("SELECT * FROM profile_photos WHERE userId = :userId ORDER BY sortOrder ASC")
    fun getPhotosForUser(userId: String): Flow<List<ProfilePhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: ProfilePhoto)

    @Update
    suspend fun updatePhoto(photo: ProfilePhoto)

    @Delete
    suspend fun deletePhoto(photo: ProfilePhoto)

    @Query("DELETE FROM profile_photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: String)
}
