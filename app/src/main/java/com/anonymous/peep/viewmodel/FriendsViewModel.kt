package com.anonymous.peep.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anonymous.peep.data.model.FriendRequestWithProfile
import com.anonymous.peep.data.model.Profile
import com.anonymous.peep.data.repository.AuthRepository
import com.anonymous.peep.data.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendsUiState(
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Profile> = emptyList(),
    val pendingRequests: List<FriendRequestWithProfile> = emptyList(),
    val friends: List<Profile> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val app: Application,
    private val authRepository: AuthRepository,
    private val friendRepository: FriendRepository,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(FriendsUiState())
    val state: StateFlow<FriendsUiState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun loadData() {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val requests = friendRepository.fetchPendingRequests(userId)
                val friends = friendRepository.fetchFriends(userId)
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    pendingRequests = requests,
                    friends = friends,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load friend data",
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        
        searchJob?.cancel()
        if (query.length < 1) {
            _state.value = _state.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }

        searchJob = viewModelScope.launch {
            _state.value = _state.value.copy(isSearching = true)
            delay(400) // debounce
            val userId = authRepository.currentUserId ?: return@launch
            val friendIds = _state.value.friends.map { it.id }
            
            try {
                val results = friendRepository.searchUsers(userId, query, friendIds)
                _state.value = _state.value.copy(
                    isSearching = false,
                    searchResults = results,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSearching = false,
                    error = e.message ?: "Search failed",
                )
            }
        }
    }

    fun sendFriendRequest(username: String) {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, successMessage = null)
            val result = friendRepository.sendFriendRequest(userId, username)
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "Friend request sent!",
                    searchQuery = "",
                    searchResults = emptyList(),
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to send request",
                )
            }
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val success = friendRepository.acceptFriendRequest(requestId)
            if (success) {
                loadData() // refresh list
                _state.value = _state.value.copy(successMessage = "Friend request accepted!")
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to accept request",
                )
            }
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val success = friendRepository.rejectFriendRequest(requestId)
            if (success) {
                loadData() // refresh list
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to reject request",
                )
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }
}
