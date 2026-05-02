package com.huabu.app.ui.screens.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.FirebaseService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrivacyUiState(
    val privateAccount: Boolean = false,
    val showOnlineStatus: Boolean = true,
    val appearInSearch: Boolean = true,
    val allowMessagesFromAnyone: Boolean = true,
    val allowFriendRequests: Boolean = true,
    val allowComments: Boolean = true,
    val defaultVisibility: String = "public",
    val shareUsageData: Boolean = false,
    val personalisedNotifications: Boolean = true,
    val isSaving: Boolean = false,
    val savedMessage: String? = null
)

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val firebaseService: FirebaseService,
    private val authService: AuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    init { loadSettings() }

    private fun loadSettings() {
        val userId = authService.getCurrentUserId() ?: return
        viewModelScope.launch {
            val user = firebaseService.getUser(userId).getOrNull() ?: return@launch
            _uiState.update {
                it.copy(
                    privateAccount = user.isPrivate,
                    defaultVisibility = user.defaultPostVisibility.ifEmpty { "public" }
                )
            }
        }
    }

    private fun save(block: PrivacyUiState.() -> PrivacyUiState) {
        _uiState.update(block)
        val userId = authService.getCurrentUserId() ?: return
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, savedMessage = null) }
            firebaseService.updateUser(
                userId, mapOf(
                    "isPrivate" to s.privateAccount,
                    "defaultPostVisibility" to s.defaultVisibility
                )
            )
            _uiState.update { it.copy(isSaving = false, savedMessage = "Settings saved") }
            delay(2000)
            _uiState.update { it.copy(savedMessage = null) }
        }
    }

    fun setPrivateAccount(v: Boolean) = save { copy(privateAccount = v) }
    fun setShowOnlineStatus(v: Boolean) = _uiState.update { it.copy(showOnlineStatus = v) }
    fun setAppearInSearch(v: Boolean) = _uiState.update { it.copy(appearInSearch = v) }
    fun setAllowMessages(v: Boolean) = _uiState.update { it.copy(allowMessagesFromAnyone = v) }
    fun setAllowFriendRequests(v: Boolean) = _uiState.update { it.copy(allowFriendRequests = v) }
    fun setAllowComments(v: Boolean) = _uiState.update { it.copy(allowComments = v) }
    fun setDefaultVisibility(v: String) = save { copy(defaultVisibility = v) }
    fun setShareUsageData(v: Boolean) = _uiState.update { it.copy(shareUsageData = v) }
    fun setPersonalisedNotifications(v: Boolean) = _uiState.update { it.copy(personalisedNotifications = v) }
}
