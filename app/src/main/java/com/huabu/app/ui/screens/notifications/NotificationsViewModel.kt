package com.huabu.app.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.FirebaseService
import com.huabu.app.data.model.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        val userId = authService.getCurrentUserId() ?: return
        viewModelScope.launch {
            firebaseService.getNotificationsFlow(userId)
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { list ->
                    _uiState.update { it.copy(notifications = list, isLoading = false) }
                }
        }
    }

    fun markAllRead() {
        val userId = authService.getCurrentUserId() ?: return
        val unread = _uiState.value.notifications.filter { !it.read }
        viewModelScope.launch {
            unread.forEach { firebaseService.markNotificationRead(it.id) }
        }
    }

    fun markRead(notificationId: String) {
        viewModelScope.launch {
            firebaseService.markNotificationRead(notificationId)
        }
    }
}
