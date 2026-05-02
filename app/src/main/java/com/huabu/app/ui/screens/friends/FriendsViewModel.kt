package com.huabu.app.ui.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.model.Friend
import com.huabu.app.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendRequest(val docId: String, val user: User)

data class FriendsUiState(
    val friends: List<User> = emptyList(),
    val friendRequests: List<FriendRequest> = emptyList(),
    val suggestedUsers: List<User> = emptyList(),
    val pendingSentIds: Set<String> = emptySet(),
    val friendIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init {
        loadAll()
    }

    private fun loadAll() {
        val currentUserId = authService.getCurrentUserId() ?: return

        // Load accepted friends
        viewModelScope.launch {
            firebaseService.getFriendsFlow(currentUserId)
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { friends ->
                    val friendUsers = friends.mapNotNull { friend ->
                        firebaseService.getUser(friend.friendId).getOrNull()
                    }
                    val friendIds = friendUsers.map { it.id }.toSet()
                    _uiState.update { it.copy(friends = friendUsers, friendIds = friendIds, isLoading = false) }
                }
        }

        // Load incoming friend requests (keep doc ID for accept/decline)
        viewModelScope.launch {
            firebaseService.getFriendRequestsFlow(currentUserId)
                .catch { }
                .collect { requests ->
                    val friendRequests = requests.mapNotNull { req ->
                        val user = firebaseService.getUser(req.friendId).getOrNull() ?: return@mapNotNull null
                        FriendRequest(docId = req.id, user = user)
                    }
                    _uiState.update { it.copy(friendRequests = friendRequests) }
                }
        }

        // Load suggested users (all users minus self and current friends)
        viewModelScope.launch {
            val result = firebaseService.searchUsers("")
            val allUsers = result.getOrDefault(emptyList())
            val suggested = allUsers.filter { it.id != currentUserId && it.id !in _uiState.value.friendIds }
            _uiState.update { it.copy(suggestedUsers = suggested) }
        }
    }

    fun sendFriendRequest(toUserId: String) {
        val currentUserId = authService.getCurrentUserId() ?: return
        _uiState.update { it.copy(pendingSentIds = it.pendingSentIds + toUserId) }
        viewModelScope.launch {
            firebaseService.sendFriendRequest(currentUserId, toUserId)
        }
    }

    fun respondToRequest(requestId: String, accept: Boolean) {
        // Optimistically remove from UI
        _uiState.update { it.copy(friendRequests = it.friendRequests.filter { r -> r.docId != requestId }) }
        viewModelScope.launch {
            firebaseService.respondToFriendRequest(requestId, accept)
        }
    }

    fun unfriend(friendUserId: String) {
        val currentUserId = authService.getCurrentUserId() ?: return
        _uiState.update { it.copy(friends = it.friends.filter { u -> u.id != friendUserId }) }
        viewModelScope.launch {
            firebaseService.removeFriend(currentUserId, friendUserId)
        }
    }

    fun getConversationForUser(otherUserId: String, onResult: (String) -> Unit) {
        val currentUserId = authService.getCurrentUserId() ?: return
        viewModelScope.launch {
            val result = firebaseService.getOrCreateConversation(currentUserId, otherUserId)
            result.getOrNull()?.let { onResult(it) }
        }
    }
}
