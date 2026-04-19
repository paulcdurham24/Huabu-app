package com.huabu.app.data.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageService @Inject constructor() {

    private val storage = FirebaseStorage.getInstance()

    // Upload image with progress tracking
    fun uploadImage(
        uri: Uri,
        path: String,
        onProgress: (Float) -> Unit = {}
    ): Flow<Result<String>> = callbackFlow {
        val ref = storage.reference.child(path)

        val uploadTask = ref.putFile(uri)

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount
            trySend(Result.success(progress.toString())) // Progress as string
            onProgress(progress)
        }.addOnSuccessListener {
            // Get download URL
            ref.downloadUrl.addOnSuccessListener { downloadUri ->
                trySend(Result.success(downloadUri.toString()))
                close()
            }.addOnFailureListener { e ->
                trySend(Result.failure(e))
                close(e)
            }
        }.addOnFailureListener { e ->
            trySend(Result.failure(e))
            close(e)
        }

        awaitClose { uploadTask.cancel() }
    }

    // Simple upload without progress
    suspend fun uploadImage(uri: Uri, path: String): Result<String> = try {
        val ref = storage.reference.child(path)
        ref.putFile(uri).await()
        val downloadUrl = ref.downloadUrl.await()
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Upload profile picture
    suspend fun uploadProfilePicture(userId: String, uri: Uri): Result<String> {
        val path = "users/$userId/profile_${UUID.randomUUID()}.jpg"
        return uploadImage(uri, path)
    }

    // Upload background image
    suspend fun uploadBackgroundImage(userId: String, uri: Uri): Result<String> {
        val path = "users/$userId/background_${UUID.randomUUID()}.jpg"
        return uploadImage(uri, path)
    }

    // Upload post image
    suspend fun uploadPostImage(userId: String, postId: String, uri: Uri): Result<String> {
        val path = "posts/$userId/$postId/image_${UUID.randomUUID()}.jpg"
        return uploadImage(uri, path)
    }

    // Upload message attachment
    suspend fun uploadMessageAttachment(
        conversationId: String,
        messageId: String,
        uri: Uri
    ): Result<String> {
        val path = "messages/$conversationId/$messageId/attachment_${UUID.randomUUID()}.jpg"
        return uploadImage(uri, path)
    }

    // Delete image
    suspend fun deleteImage(imageUrl: String): Result<Unit> = try {
        val ref = storage.getReferenceFromUrl(imageUrl)
        ref.delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Get storage reference for custom paths
    fun getReference(path: String): StorageReference {
        return storage.reference.child(path)
    }

    // Batch delete images
    suspend fun deleteImages(imageUrls: List<String>): Result<Unit> = try {
        imageUrls.forEach { url ->
            try {
                val ref = storage.getReferenceFromUrl(url)
                ref.delete().await()
            } catch (e: Exception) {
                // Continue even if one fails
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
