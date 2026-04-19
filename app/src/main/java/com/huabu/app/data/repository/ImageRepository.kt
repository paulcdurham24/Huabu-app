package com.huabu.app.data.repository

import android.content.Context
import android.net.Uri
import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.firebase.StorageService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    private val storageService: StorageService,
    private val firebaseService: FirebaseService,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    @ApplicationContext private val context: Context
) {
    // Upload profile picture and update user
    suspend fun updateProfilePicture(userId: String, imageUri: Uri): Result<String> {
        return try {
            // Upload to Storage
            val uploadResult = storageService.uploadProfilePicture(userId, imageUri)

            uploadResult.fold(
                onSuccess = { downloadUrl ->
                    // Update user document with new avatar URL
                    val updateResult = firebaseService.updateUser(
                        userId,
                        mapOf("avatarUrl" to downloadUrl)
                    )

                    updateResult.fold(
                        onSuccess = { Result.success(downloadUrl) },
                        onFailure = { Result.failure(it) }
                    )
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload background image and update user
    suspend fun updateBackgroundImage(userId: String, imageUri: Uri): Result<String> {
        return try {
            val uploadResult = storageService.uploadBackgroundImage(userId, imageUri)

            uploadResult.fold(
                onSuccess = { downloadUrl ->
                    val updateResult = firebaseService.updateUser(
                        userId,
                        mapOf("backgroundImageUrl" to downloadUrl)
                    )

                    updateResult.fold(
                        onSuccess = { Result.success(downloadUrl) },
                        onFailure = { Result.failure(it) }
                    )
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload with progress tracking
    fun uploadProfilePictureWithProgress(
        userId: String,
        imageUri: Uri
    ): Flow<UploadProgress> = flow {
        emit(UploadProgress.Loading(0f))

        var finalUrl: String? = null

        storageService.uploadImage(imageUri, "users/$userId/profile_${System.currentTimeMillis()}.jpg") { progress ->
            // This is called during upload
        }.collect { result ->
            result.fold(
                onSuccess = { value ->
                    // Check if this is progress or final URL
                    if (value.startsWith("http")) {
                        finalUrl = value
                        // Update user profile
                        firebaseService.updateUser(userId, mapOf("avatarUrl" to value))
                        emit(UploadProgress.Success(value))
                    } else {
                        // It's progress
                        emit(UploadProgress.Loading(value.toFloatOrNull() ?: 0f))
                    }
                },
                onFailure = { error ->
                    emit(UploadProgress.Error(error.message ?: "Upload failed"))
                }
            )
        }
    }

    // Delete profile picture
    suspend fun deleteProfilePicture(userId: String, currentImageUrl: String): Result<Unit> {
        return try {
            // Delete from storage
            val deleteResult = storageService.deleteImage(currentImageUrl)

            // Update user to remove avatar URL regardless of storage delete result
            firebaseService.updateUser(userId, mapOf("avatarUrl" to ""))

            deleteResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cache image locally
    suspend fun cacheImage(imageUrl: String, fileName: String): File? {
        return try {
            val cacheDir = File(context.cacheDir, "images")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val file = File(cacheDir, fileName)

            // Download and save
            val url = java.net.URL(imageUrl)
            url.openStream().use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    // Get cached image or download
    suspend fun getCachedImage(imageUrl: String): File? {
        val fileName = imageUrl.hashCode().toString() + ".jpg"
        val cacheDir = File(context.cacheDir, "images")
        val cachedFile = File(cacheDir, fileName)

        return if (cachedFile.exists()) {
            cachedFile
        } else {
            cacheImage(imageUrl, fileName)
        }
    }

    // Clear image cache
    fun clearImageCache(): Result<Unit> = try {
        val cacheDir = File(context.cacheDir, "images")
        cacheDir.listFiles()?.forEach { it.delete() }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Upload progress states
sealed class UploadProgress {
    data class Loading(val progress: Float) : UploadProgress()
    data class Success(val downloadUrl: String) : UploadProgress()
    data class Error(val message: String) : UploadProgress()
}
