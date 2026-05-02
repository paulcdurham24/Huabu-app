package com.huabu.app.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.model.User
import com.huabu.app.data.repository.ImageRepository
import com.huabu.app.data.repository.UploadProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val avatarUploadProgress: Float = 0f,
    val backgroundUploadProgress: Float = 0f,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val imageRepository: ImageRepository,
    private val firebaseService: FirebaseService
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = firebaseService.getUser(userId)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message,
                        isLoading = false
                    )
                }
            )
        }
    }

    fun updateAvatar(userId: String, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(avatarUploadProgress = 0f)
            android.util.Log.d("EditProfile", "updateAvatar called with uri: $imageUri")

            val result = imageRepository.updateProfilePicture(userId, imageUri)
            android.util.Log.d("EditProfile", "upload result: $result")
            result.fold(
                onSuccess = { downloadUrl ->
                    android.util.Log.d("EditProfile", "upload success: $downloadUrl")
                    _uiState.value = _uiState.value.copy(
                        user = _uiState.value.user?.copy(profileImageUrl = downloadUrl),
                        avatarUploadProgress = 1f
                    )
                },
                onFailure = { error ->
                    android.util.Log.e("EditProfile", "upload failed: ${error.message}", error)
                    val msg = when {
                        error.message?.contains("404") == true -> "Storage not configured. Please enable Firebase Storage in console."
                        else -> "Failed to upload avatar: ${error.message}"
                    }
                    _uiState.value = _uiState.value.copy(
                        errorMessage = msg,
                        avatarUploadProgress = 0f
                    )
                }
            )
        }
    }

    fun updateBackground(userId: String, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(backgroundUploadProgress = 0f)

            val result = imageRepository.updateBackgroundImage(userId, imageUri)
            result.fold(
                onSuccess = { downloadUrl ->
                    _uiState.value = _uiState.value.copy(
                        user = _uiState.value.user?.copy(backgroundImageUrl = downloadUrl),
                        backgroundUploadProgress = 1f
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to upload background: ${error.message}",
                        backgroundUploadProgress = 0f
                    )
                }
            )
        }
    }

    fun saveProfile(userId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            val result = firebaseService.updateUser(userId, updates)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Failed to save: ${error.message}"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun onSaveComplete() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}
