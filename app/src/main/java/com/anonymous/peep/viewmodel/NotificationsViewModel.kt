package com.anonymous.peep.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anonymous.peep.data.model.PeepWithProfile
import com.anonymous.peep.data.repository.AuthRepository
import com.anonymous.peep.data.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<PeepWithProfile> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val app: Application,
    private val authRepository: AuthRepository,
    private val friendRepository: FriendRepository,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    fun loadNotifications() {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val peeps = friendRepository.fetchNotifications(userId)
                _state.value = _state.value.copy(
                    isLoading = false,
                    notifications = peeps,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load notifications",
                )
            }
        }
    }
}
