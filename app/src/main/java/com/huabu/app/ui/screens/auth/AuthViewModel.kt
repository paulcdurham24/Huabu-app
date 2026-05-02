package com.huabu.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.AuthState
import com.huabu.app.data.firebase.FirebaseService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false
)

data class SignupUiState(
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val signupSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService,
    private val firebaseService: FirebaseService
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _signupState = MutableStateFlow(SignupUiState())
    val signupState: StateFlow<SignupUiState> = _signupState.asStateFlow()

    val authState = authService.authState
    val currentUser = authService.currentUser

    fun onLoginEmailChange(email: String) {
        _loginState.value = _loginState.value.copy(email = email, errorMessage = null)
    }

    fun onLoginPasswordChange(password: String) {
        _loginState.value = _loginState.value.copy(password = password, errorMessage = null)
    }

    fun login() {
        val state = _loginState.value

        // Validation
        when {
            state.email.isBlank() -> {
                _loginState.value = state.copy(errorMessage = "Please enter your email")
                return
            }
            state.password.isBlank() -> {
                _loginState.value = state.copy(errorMessage = "Please enter your password")
                return
            }
            state.password.length < 6 -> {
                _loginState.value = state.copy(errorMessage = "Password must be at least 6 characters")
                return
            }
        }

        viewModelScope.launch {
            _loginState.value = state.copy(isLoading = true, errorMessage = null)

            val result = authService.signInWithEmail(state.email, state.password)

            result.fold(
                onSuccess = { userId ->
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        loginSuccess = true
                    )
                    registerFcmToken(userId)
                },
                onFailure = { error ->
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Login failed. Please try again."
                    )
                }
            )
        }
    }

    fun onSignupDisplayNameChange(name: String) {
        _signupState.value = _signupState.value.copy(displayName = name, errorMessage = null)
    }

    fun onSignupUsernameChange(username: String) {
        _signupState.value = _signupState.value.copy(username = username, errorMessage = null)
    }

    fun onSignupEmailChange(email: String) {
        _signupState.value = _signupState.value.copy(email = email, errorMessage = null)
    }

    fun onSignupPasswordChange(password: String) {
        _signupState.value = _signupState.value.copy(password = password, errorMessage = null)
    }

    fun onSignupConfirmPasswordChange(password: String) {
        _signupState.value = _signupState.value.copy(confirmPassword = password, errorMessage = null)
    }

    fun signup() {
        val state = _signupState.value
        Log.d("AuthViewModel", "signup() called with email=${state.email}, displayName=${state.displayName}")

        // Validation
        when {
            state.displayName.isBlank() -> {
                Log.d("AuthViewModel", "Validation failed: display name blank")
                _signupState.value = state.copy(errorMessage = "Please enter your display name")
                return
            }
            state.username.isBlank() -> {
                Log.d("AuthViewModel", "Validation failed: username blank")
                _signupState.value = state.copy(errorMessage = "Please enter a username")
                return
            }
            state.email.isBlank() -> {
                Log.d("AuthViewModel", "Validation failed: email blank")
                _signupState.value = state.copy(errorMessage = "Please enter your email")
                return
            }
            state.password.isBlank() -> {
                Log.d("AuthViewModel", "Validation failed: password blank")
                _signupState.value = state.copy(errorMessage = "Please enter a password")
                return
            }
            state.password.length < 6 -> {
                Log.d("AuthViewModel", "Validation failed: password too short")
                _signupState.value = state.copy(errorMessage = "Password must be at least 6 characters")
                return
            }
            state.password != state.confirmPassword -> {
                Log.d("AuthViewModel", "Validation failed: passwords don't match")
                _signupState.value = state.copy(errorMessage = "Passwords do not match")
                return
            }
        }
        Log.d("AuthViewModel", "Validation passed, calling authService.signUpWithEmail")

        viewModelScope.launch {
            _signupState.value = state.copy(isLoading = true, errorMessage = null)

            val result = authService.signUpWithEmail(
                email = state.email,
                password = state.password,
                displayName = state.displayName,
                username = state.username
            )

            Log.d("AuthViewModel", "Signup result: $result")
            result.fold(
                onSuccess = { userId ->
                    Log.d("AuthViewModel", "Signup success! userId=$userId, setting signupSuccess=true")
                    _signupState.value = _signupState.value.copy(
                        isLoading = false,
                        signupSuccess = true
                    )
                    Log.d("AuthViewModel", "State after update: ${_signupState.value}")
                    registerFcmToken(userId)
                },
                onFailure = { error ->
                    Log.d("AuthViewModel", "Signup failed: ${error.message}")
                    val errorMsg = when {
                        error.message?.contains("Username already taken") == true -> "That username is already taken. Please choose another."
                        error.message?.contains("email") == true -> "Invalid email format or email already in use"
                        error.message?.contains("password") == true -> "Password is too weak. Use at least 6 characters"
                        error.message?.contains("network") == true -> "Network error. Please check your connection"
                        else -> error.message ?: "Signup failed. Please try again."
                    }
                    _signupState.value = _signupState.value.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authService.signOut()
        }
    }

    fun sendPasswordReset(email: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = authService.sendPasswordResetEmail(email)
            onResult(result)
        }
    }

    fun changePassword(newPassword: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = authService.updatePassword(newPassword)
            onResult(result)
        }
    }

    fun deleteAccount(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = authService.deleteAccount()
            onResult(result)
        }
    }

    fun isEmailVerified(): Boolean = authService.isEmailVerified()

    fun resendVerificationEmail(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = authService.resendVerificationEmail()
            onResult(result)
        }
    }

    fun clearLoginError() {
        _loginState.value = _loginState.value.copy(errorMessage = null)
    }

    fun clearSignupError() {
        _signupState.value = _signupState.value.copy(errorMessage = null)
    }

    fun onLoginSuccessHandled() {
        _loginState.value = _loginState.value.copy(loginSuccess = false)
    }

    fun onSignupSuccessHandled() {
        _signupState.value = _signupState.value.copy(signupSuccess = false)
    }

    private fun registerFcmToken(userId: String) {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                firebaseService.saveFcmToken(userId, token)
            } catch (_: Exception) {}
        }
    }
}
