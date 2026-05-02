package com.huabu.app.ui.screens.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.model.Conversation
import com.huabu.app.data.model.ConversationUI
import com.huabu.app.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessagesUiState(
    val conversations: List<ConversationUI> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        val currentUserId = authService.getCurrentUserId() ?: return

        viewModelScope.launch {
            firebaseService.getConversationsFlow(currentUserId)
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { conversations ->
                    val uiConvos = conversations.mapNotNull { convo ->
                        val otherUserId = convo.participantIds.firstOrNull { it != currentUserId } ?: return@mapNotNull null
                        val user = firebaseService.getUser(otherUserId).getOrNull() ?: User(id = otherUserId, displayName = "Unknown")
                        ConversationUI(
                            conversationId = convo.id,
                            otherUser = user,
                            lastMessage = convo.lastMessage,
                            lastMessageTimestamp = convo.lastMessageTimestamp,
                            unreadCount = convo.unreadCount
                        )
                    }
                    _uiState.update { it.copy(conversations = uiConvos, isLoading = false) }
                }
        }
    }

    fun getOrCreateConversation(otherUserId: String, onResult: (String) -> Unit) {
        val currentUserId = authService.getCurrentUserId() ?: return
        viewModelScope.launch {
            val result = firebaseService.getOrCreateConversation(currentUserId, otherUserId)
            result.getOrNull()?.let { onResult(it) }
        }
    }
}
