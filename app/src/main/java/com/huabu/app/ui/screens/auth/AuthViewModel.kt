package com.huabu.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huabu.app.data.firebase.AuthService
import com.huabu.app.data.firebase.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val authService: AuthService
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
                    _loginState.value = state.copy(
                        isLoading = false,
                        loginSuccess = true
                    )
                },
                onFailure = { error ->
                    _loginState.value = state.copy(
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

        // Validation
        when {
            state.displayName.isBlank() -> {
                _signupState.value = state.copy(errorMessage = "Please enter your display name")
                return
            }
            state.username.isBlank() -> {
                _signupState.value = state.copy(errorMessage = "Please enter a username")
                return
            }
            state.email.isBlank() -> {
                _signupState.value = state.copy(errorMessage = "Please enter your email")
                return
            }
            state.password.isBlank() -> {
                _signupState.value = state.copy(errorMessage = "Please enter a password")
                return
            }
            state.password.length < 6 -> {
                _signupState.value = state.copy(errorMessage = "Password must be at least 6 characters")
                return
            }
            state.password != state.confirmPassword -> {
                _signupState.value = state.copy(errorMessage = "Passwords do not match")
                return
            }
        }

        viewModelScope.launch {
            _signupState.value = state.copy(isLoading = true, errorMessage = null)

            val result = authService.signUpWithEmail(
                email = state.email,
                password = state.password,
                displayName = state.displayName,
                username = state.username
            )

            result.fold(
                onSuccess = { userId ->
                    _signupState.value = state.copy(
                        isLoading = false,
                        signupSuccess = true
                    )
                },
                onFailure = { error ->
                    val errorMsg = when {
                        error.message?.contains("email address is already in use") == true ->
                            "An account with this email already exists"
                        error.message?.contains(" badly formatted") == true ->
                            "Please enter a valid email address"
                        error.message?.contains("password is invalid") == true ->
                            "Password is too weak. Use at least 6 characters"
                        else -> error.message ?: "Signup failed. Please try again."
                    }
                    _signupState.value = state.copy(
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
}
