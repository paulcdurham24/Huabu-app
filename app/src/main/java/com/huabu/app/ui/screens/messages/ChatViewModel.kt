package com.huabu.app.ui.screens.messages

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.firebase.StorageService
import com.huabu.app.data.model.Message
import com.huabu.app.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val otherUser: User? = null,
    val currentUserId: String = "",
    val currentUserName: String = "",
    val currentUserImageUrl: String = "",
    val isMuted: Boolean = false,
    val isSending: Boolean = false,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMoreMessages: Boolean = true,
    val otherUserTyping: Boolean = false,
    val isRecording: Boolean = false,
    val recordingSeconds: Int = 0,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
    private val storageService: StorageService,
    private val authService: AuthService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    private val pageSize = 30L
    private var stopTypingJob: Job? = null
    private var conversationId: String = ""
    private var mediaRecorder: MediaRecorder? = null
    private var voiceFile: File? = null
    private var recordStartMs: Long = 0L
    private var recordingTimerJob: Job? = null

    fun loadChat(convId: String) {
        conversationId = convId
        val currentUserId = authService.getCurrentUserId() ?: return
        _uiState.update { it.copy(currentUserId = currentUserId) }

        // Load current user info for message metadata
        viewModelScope.launch {
            firebaseService.getUser(currentUserId).getOrNull()?.let { me ->
                _uiState.update { it.copy(currentUserName = me.displayName, currentUserImageUrl = me.profileImageUrl) }
            }
        }

        // Mark messages as read immediately on open
        viewModelScope.launch { firebaseService.markConversationRead(convId) }

        viewModelScope.launch {
            firebaseService.getConversationsFlow(currentUserId)
                .catch { }
                .collect { convos ->
                    val convo = convos.firstOrNull { it.id == convId } ?: return@collect
                    val otherUserId = convo.participantIds.firstOrNull { it != currentUserId } ?: return@collect
                    val user = firebaseService.getUser(otherUserId).getOrNull()
                    _uiState.update { it.copy(otherUser = user) }
                    // Start typing indicator listener once we know the other user
                    viewModelScope.launch {
                        firebaseService.getTypingFlow(convId, otherUserId)
                            .catch { }
                            .collect { typing -> _uiState.update { it.copy(otherUserTyping = typing) } }
                    }
                }
        }

        viewModelScope.launch {
            firebaseService.getMessagesFlow(convId)
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { messages ->
                    _uiState.update { it.copy(
                        messages = messages,
                        isLoading = false,
                        hasMoreMessages = messages.size >= pageSize
                    )}
                }
        }
    }

    fun loadMoreMessages() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMoreMessages || conversationId.isEmpty()) return
        val oldest = state.messages.minByOrNull { it.timestamp }?.timestamp ?: return
        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            val result = firebaseService.getOlderMessages(conversationId, oldest, pageSize)
            result.onSuccess { older ->
                _uiState.update { it.copy(
                    messages = (older + it.messages).distinctBy { m -> m.id },
                    isLoadingMore = false,
                    hasMoreMessages = older.size >= pageSize
                )}
            }.onFailure {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun onTyping() {
        val uid = authService.getCurrentUserId() ?: return
        if (conversationId.isEmpty()) return
        stopTypingJob?.cancel()
        viewModelScope.launch { firebaseService.setTypingStatus(conversationId, uid, true) }
        stopTypingJob = viewModelScope.launch {
            delay(5_000L)
            firebaseService.setTypingStatus(conversationId, uid, false)
        }
    }

    fun stopTyping() {
        val uid = authService.getCurrentUserId() ?: return
        if (conversationId.isEmpty()) return
        stopTypingJob?.cancel()
        viewModelScope.launch { firebaseService.setTypingStatus(conversationId, uid, false) }
    }

    fun sendMessage(conversationId: String, text: String) {
        val currentUserId = authService.getCurrentUserId() ?: return
        val otherUserId = _uiState.value.otherUser?.id ?: return
        if (text.isBlank()) return

        _uiState.update { it.copy(isSending = true) }

        viewModelScope.launch {
            val s = _uiState.value
            val message = Message(
                conversationId = conversationId,
                senderId = currentUserId,
                receiverId = otherUserId,
                senderName = s.currentUserName,
                senderImageUrl = s.currentUserImageUrl,
                content = text.trim(),
                timestamp = System.currentTimeMillis()
            )
            firebaseService.sendMessage(conversationId, message)
            _uiState.update { it.copy(isSending = false) }
        }
    }

    fun deleteMessage(conversationId: String, messageId: String) {
        _uiState.update { s -> s.copy(messages = s.messages.filter { it.id != messageId }) }
        viewModelScope.launch { firebaseService.deleteMessage(conversationId, messageId) }
    }

    fun sendImageMessage(conversationId: String, imageUri: Uri) {
        val currentUserId = authService.getCurrentUserId() ?: return
        val otherUserId = _uiState.value.otherUser?.id ?: return
        _uiState.update { it.copy(isSending = true) }
        viewModelScope.launch {
            val s = _uiState.value
            val tempId = java.util.UUID.randomUUID().toString()
            val uploadResult = storageService.uploadMessageAttachment(conversationId, tempId, imageUri)
            if (uploadResult.isFailure) { _uiState.update { it.copy(isSending = false) }; return@launch }
            val imageUrl = uploadResult.getOrDefault("")
            val message = Message(
                conversationId = conversationId,
                senderId = currentUserId,
                receiverId = otherUserId,
                senderName = s.currentUserName,
                senderImageUrl = s.currentUserImageUrl,
                content = imageUrl,
                timestamp = System.currentTimeMillis()
            )
            firebaseService.sendMessage(conversationId, message)
            _uiState.update { it.copy(isSending = false) }
        }
    }

    fun startRecording() {
        val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        voiceFile = file
        recordStartMs = System.currentTimeMillis()
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION") MediaRecorder()
        }
        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        _uiState.update { it.copy(isRecording = true, recordingSeconds = 0) }
        recordingTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(recordingSeconds = it.recordingSeconds + 1) }
            }
        }
    }

    fun cancelRecording() {
        recordingTimerJob?.cancel()
        mediaRecorder?.runCatching { stop(); release() }
        mediaRecorder = null
        voiceFile?.delete()
        voiceFile = null
        _uiState.update { it.copy(isRecording = false, recordingSeconds = 0) }
    }

    fun stopAndSendVoice(convId: String) {
        recordingTimerJob?.cancel()
        val durationMs = System.currentTimeMillis() - recordStartMs
        mediaRecorder?.runCatching { stop(); release() }
        mediaRecorder = null
        val file = voiceFile ?: run { _uiState.update { it.copy(isRecording = false) }; return }
        voiceFile = null
        _uiState.update { it.copy(isRecording = false, recordingSeconds = 0, isSending = true) }
        val currentUserId = authService.getCurrentUserId() ?: return
        val otherUserId = _uiState.value.otherUser?.id ?: return
        viewModelScope.launch {
            val s = _uiState.value
            val uploadResult = storageService.uploadVoiceMessage(convId, file)
            file.delete()
            if (uploadResult.isFailure) { _uiState.update { it.copy(isSending = false) }; return@launch }
            val voiceUrl = uploadResult.getOrDefault("")
            val message = Message(
                conversationId = convId,
                senderId = currentUserId,
                receiverId = otherUserId,
                senderName = s.currentUserName,
                senderImageUrl = s.currentUserImageUrl,
                content = "",
                voiceUrl = voiceUrl,
                voiceDurationMs = durationMs,
                timestamp = System.currentTimeMillis()
            )
            firebaseService.sendMessage(convId, message)
            _uiState.update { it.copy(isSending = false) }
        }
    }

    fun toggleMute() {
        _uiState.update { it.copy(isMuted = !it.isMuted) }
    }

    override fun onCleared() {
        super.onCleared()
        mediaRecorder?.runCatching { stop(); release() }
    }
}
