package com.anonymous.peep.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anonymous.peep.data.model.Profile
import com.anonymous.peep.data.repository.AuthRepository
import com.anonymous.peep.service.StatusBroadcastService
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isInitialized: Boolean = false,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userId: String? = null,
    val profile: Profile? = null,
    val error: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val app: Application,
    private val authRepository: AuthRepository,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val hasSession = authRepository.initialize()
            if (hasSession) {
                val profile = authRepository.fetchProfile()
                _state.value = _state.value.copy(
                    isInitialized = true,
                    isLoading = false,
                    isLoggedIn = true,
                    userId = authRepository.currentUserId,
                    profile = profile,
                )
                registerFcmToken()
            } else {
                _state.value = _state.value.copy(
                    isInitialized = true,
                    isLoading = false,
                    isLoggedIn = false,
                )
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = authRepository.signIn(email, password)
            if (result.isSuccess) {
                val profile = authRepository.fetchProfile()
                _state.value = _state.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    userId = result.getOrNull(),
                    profile = profile,
                    error = null,
                )
                registerFcmToken()
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Sign in failed",
                )
            }
        }
    }

    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = authRepository.signUp(email, password, username)
            if (result.isSuccess) {
                // Wait a moment for the database trigger to create the profile
                kotlinx.coroutines.delay(500)
                val profile = authRepository.fetchProfile()
                _state.value = _state.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    userId = result.getOrNull(),
                    profile = profile,
                    error = null,
                )
                registerFcmToken()
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Sign up failed",
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            StatusBroadcastService.stop(app)
            authRepository.clearFcmToken()
            authRepository.signOut()
            _state.value = AuthUiState(isInitialized = true)
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun registerFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                viewModelScope.launch {
                    authRepository.updateFcmToken(token)
                }
            }
        }
    }
}
