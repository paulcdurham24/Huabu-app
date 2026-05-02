package com.huabu.app.ui.screens.compose

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.firebase.StorageService
import com.huabu.app.data.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.draftDataStore by preferencesDataStore(name = "post_drafts")
private val DRAFT_CONTENT = stringPreferencesKey("draft_content")
private val DRAFT_MOOD = stringPreferencesKey("draft_mood")
private val DRAFT_TAGS = stringPreferencesKey("draft_tags")

data class ComposePostUiState(
    val authorName: String = "",
    val authorUsername: String = "",
    val authorImageUrl: String = "",
    val isSubmitting: Boolean = false,
    val uploadProgress: Float = 0f,
    val submitSuccess: Boolean = false,
    val error: String? = null,
    val editPostId: String? = null,
    val initialContent: String = "",
    val initialMood: String = "",
    val initialTags: String = "",
    val initialVisibility: String = "public",
    val hasDraft: Boolean = false
)

@HiltViewModel
class ComposePostViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
    private val storageService: StorageService,
    private val authService: AuthService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComposePostUiState())
    val uiState: StateFlow<ComposePostUiState> = _uiState.asStateFlow()

    private val _draftContent = MutableStateFlow("")
    private val _draftMood = MutableStateFlow("")
    private val _draftTags = MutableStateFlow("")

    init {
        loadCurrentUser()
        loadDraft()
        observeAndSaveDraft()
    }

    private fun loadDraft() {
        viewModelScope.launch {
            context.draftDataStore.data.first().let { prefs ->
                val content = prefs[DRAFT_CONTENT] ?: ""
                val mood = prefs[DRAFT_MOOD] ?: ""
                val tags = prefs[DRAFT_TAGS] ?: ""
                if (content.isNotEmpty()) {
                    _uiState.update { it.copy(initialContent = content, initialMood = mood, initialTags = tags, hasDraft = true) }
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeAndSaveDraft() {
        viewModelScope.launch {
            combine(_draftContent, _draftMood, _draftTags) { c, m, t -> Triple(c, m, t) }
                .debounce(1500)
                .collect { (content, mood, tags) ->
                    if (_uiState.value.editPostId == null) {
                        context.draftDataStore.edit { prefs ->
                            prefs[DRAFT_CONTENT] = content
                            prefs[DRAFT_MOOD] = mood
                            prefs[DRAFT_TAGS] = tags
                        }
                        _uiState.update { it.copy(hasDraft = content.isNotEmpty()) }
                    }
                }
        }
    }

    fun onDraftChanged(content: String, mood: String, tags: String) {
        _draftContent.value = content
        _draftMood.value = mood
        _draftTags.value = tags
    }

    fun clearDraft() {
        viewModelScope.launch {
            context.draftDataStore.edit { it.clear() }
            _uiState.update { it.copy(hasDraft = false) }
        }
    }

    private fun loadCurrentUser() {
        val userId = authService.getCurrentUserId() ?: return
        viewModelScope.launch {
            val result = firebaseService.getUser(userId)
            result.getOrNull()?.let { user ->
                _uiState.value = _uiState.value.copy(
                    authorName = user.displayName,
                    authorUsername = user.username,
                    authorImageUrl = user.profileImageUrl
                )
            }
        }
    }

    fun submitPost(
        content: String,
        mood: String,
        tags: String,
        visibility: String = "public",
        imageUri: Uri? = null
    ) {
        val userId = authService.getCurrentUserId() ?: return
        val state = _uiState.value
        if (content.isBlank()) return

        _uiState.value = state.copy(isSubmitting = true, uploadProgress = 0f, error = null)

        viewModelScope.launch {
            var imageUrl = ""

            if (imageUri != null) {
                val tempPostId = java.util.UUID.randomUUID().toString()
                val uploadResult = storageService.uploadPostImage(userId, tempPostId, imageUri)
                if (uploadResult.isSuccess) {
                    imageUrl = uploadResult.getOrDefault("")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = "Image upload failed: ${uploadResult.exceptionOrNull()?.message}"
                    )
                    return@launch
                }
            }

            val post = Post(
                authorId = userId,
                authorName = state.authorName,
                authorUsername = state.authorUsername,
                authorImageUrl = state.authorImageUrl,
                content = content.trim(),
                imageUrl = imageUrl,
                mood = mood,
                tags = tags.trim(),
                createdAt = System.currentTimeMillis(),
                visibility = visibility
            )
            val result = firebaseService.createPost(post)
            if (result.isSuccess) {
                clearDraft()
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to post"
                )
            }
        }
    }

    fun onSubmitHandled() {
        _uiState.value = _uiState.value.copy(submitSuccess = false)
    }

    fun loadPostForEdit(postId: String) {
        viewModelScope.launch {
            val post = firebaseService.getPost(postId).getOrNull() ?: return@launch
            _uiState.value = _uiState.value.copy(
                editPostId = postId,
                initialContent = post.content,
                initialMood = post.mood,
                initialTags = post.tags,
                initialVisibility = post.visibility
            )
        }
    }

    fun editPost(
        postId: String,
        content: String,
        mood: String,
        tags: String,
        visibility: String
    ) {
        if (content.isBlank()) return
        _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
        viewModelScope.launch {
            val result = firebaseService.updatePost(postId, content.trim(), mood, tags.trim(), visibility)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, submitSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to update post"
                )
            }
        }
    }
}
