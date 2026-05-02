package com.huabu.app.data.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.huabu.app.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor() {

    private val auth = FirebaseAuth.getInstance()
    private val firebaseService = FirebaseService()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        // Listen to auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            _authState.value = if (firebaseAuth.currentUser != null) {
                AuthState.Authenticated(firebaseAuth.currentUser!!.uid)
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun isEmailVerified(): Boolean = auth.currentUser?.isEmailVerified == true

    suspend fun resendVerificationEmail(): Result<Unit> = try {
        auth.currentUser?.sendEmailVerification()?.await()
            ?: throw Exception("No user logged in")
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun signInWithEmail(email: String, password: String): Result<String> = try {
        _authState.value = AuthState.Loading
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val userId = result.user?.uid
            ?: throw Exception("Login failed: No user ID returned")
        _authState.value = AuthState.Authenticated(userId)
        Result.success(userId)
    } catch (e: Exception) {
        _authState.value = AuthState.Error(e.message ?: "Login failed")
        Result.failure(e)
    }

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        username: String
    ): Result<String> {
        return try {
        _authState.value = AuthState.Loading
        Log.d("AuthService", "Starting signup for email=$email")

        // Check username uniqueness before creating the account
        val normalizedUsername = username.lowercase().replace(" ", "_")
        if (firebaseService.isUsernameTaken(normalizedUsername)) {
            _authState.value = AuthState.Error("Username already taken")
            return Result.failure(Exception("Username already taken"))
        }

        // Create Firebase Auth user
        Log.d("AuthService", "Calling createUserWithEmailAndPassword...")
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = result.user?.uid
            ?: throw Exception("Signup failed: No user ID returned")
        Log.d("AuthService", "Firebase Auth user created: userId=$userId")

        // Update profile with display name
        Log.d("AuthService", "Updating profile...")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        result.user?.updateProfile(profileUpdates)?.await()
        Log.d("AuthService", "Profile updated")

        // Send email verification
        try { result.user?.sendEmailVerification()?.await() } catch (_: Exception) {}

        // Create user document in Firestore (non-blocking - auth already succeeded)
        Log.d("AuthService", "Creating Firestore user document...")
        val newUser = User(
            id = userId,
            username = username.lowercase().replace(" ", "_"),
            displayName = displayName,
            email = email
        )
        try {
            val firestoreResult = withTimeout(5000) {
                firebaseService.createUser(userId, newUser)
            }
            Log.d("AuthService", "Firestore result: $firestoreResult")
        } catch (e: Exception) {
            Log.d("AuthService", "Firestore failed (will retry later): ${e.message}")
            // Firestore failed but auth succeeded - still complete signup
        }

        _authState.value = AuthState.Authenticated(userId)
        Log.d("AuthService", "Signup complete!")
        Result.success(userId)
        } catch (e: Exception) {
            Log.d("AuthService", "Signup failed with exception: ${e.message}")
            _authState.value = AuthState.Error(e.message ?: "Signup failed")
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> = try {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        _currentUser.value = null
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updatePassword(newPassword: String): Result<Unit> = try {
        auth.currentUser?.updatePassword(newPassword)?.await()
            ?: throw Exception("No user logged in")
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteAccount(): Result<Unit> = try {
        val userId = auth.currentUser?.uid
            ?: throw Exception("No user logged in")

        // Delete user data from Firestore first
        // Note: In production, use Cloud Functions for cleanup

        // Delete Firebase Auth account
        auth.currentUser?.delete()?.await()
            ?: throw Exception("No user logged in")

        _authState.value = AuthState.Unauthenticated
        _currentUser.value = null
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun reloadUser() {
        auth.currentUser?.reload()
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
