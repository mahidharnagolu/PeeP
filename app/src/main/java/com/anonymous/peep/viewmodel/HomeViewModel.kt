package com.anonymous.peep.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anonymous.peep.data.model.Profile
import com.anonymous.peep.data.model.UserStatus
import com.anonymous.peep.data.repository.AuthRepository
import com.anonymous.peep.data.repository.FriendRepository
import com.anonymous.peep.data.repository.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendStatusUi(
    val profile: Profile,
    val status: UserStatus? = null,
    val isPeeping: Boolean = false,
    val cooldownSeconds: Int = 0,
    val displayTimer: Int = 0, // 15 seconds to display the status
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val friends: List<FriendStatusUi> = emptyList(),
    val peepsRemaining: Int = 5,
    val error: String? = null,
    val toastMessage: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val app: Application,
    private val authRepository: AuthRepository,
    private val friendRepository: FriendRepository,
    private val statusRepository: StatusRepository,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val cooldownJobs = mutableMapOf<String, Job>()
    private val displayJobs = mutableMapOf<String, Job>()

    fun loadData() {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            // Get user's profile to know remaining peeps
            val profile = authRepository.fetchProfile()
            val peepsRemaining = profile?.dailyPeepsRemaining ?: 5

            // Get friends list
            val friends = friendRepository.fetchFriends(userId)
            val uiFriends = friends.map { FriendStatusUi(profile = it) }

            _state.value = _state.value.copy(
                isLoading = false,
                friends = uiFriends,
                peepsRemaining = peepsRemaining
            )
        }
    }

    fun peepFriend(friendId: String) {
        val userId = authRepository.currentUserId ?: return
        val currentState = _state.value
        val friendUi = currentState.friends.find { it.profile.id == friendId } ?: return

        if (currentState.peepsRemaining <= 0) {
            showToast("You're out of peeps for today!")
            return
        }

        if (friendUi.cooldownSeconds > 0 || friendUi.isPeeping) return

        Log.d(TAG, "Peeping friend: ${friendUi.profile.username} ($friendId)")

        viewModelScope.launch {
            // Set peeping state
            updateFriend(friendId) { it.copy(isPeeping = true) }

            // Deduct peep count locally
            _state.value = _state.value.copy(peepsRemaining = currentState.peepsRemaining - 1)

            // Try to get status with retries (simulate realtime wait if they were offline)
            var status: UserStatus? = null
            
            // First trigger silent push to wake up their device
            Log.d(TAG, "Triggering silent peep for $friendId")
            statusRepository.triggerSilentPeep(friendId)
            
            // Try fetching 5 times with 1 second delay
            for (i in 0..4) {
                status = friendRepository.getFriendStatus(friendId)
                Log.d(TAG, "Status fetch attempt ${i+1}/5: ${status?.friendlyName ?: "null"}")
                if (status != null) break
                delay(1000)
            }

            if (status != null) {
                Log.d(TAG, "Got status: ${status.friendlyName}")
                // We got the status!
                updateFriend(friendId) { 
                    it.copy(
                        isPeeping = false, 
                        status = status,
                        displayTimer = 15,
                        cooldownSeconds = 30
                    ) 
                }
                
                // Record the peep
                val currentProfile = authRepository.fetchProfile()
                Log.d(TAG, "Recording peep...")
                statusRepository.recordPeep(
                    fromUserId = userId,
                    toUserId = friendId,
                    detectedApp = status.currentApp,
                    friendlyName = status.friendlyName ?: "Something mysterious"
                )
                
                // Send notification
                Log.d(TAG, "Sending peep notification...")
                statusRepository.sendPeepNotification(
                    fromUserId = userId,
                    toUserId = friendId,
                    friendlyName = status.friendlyName ?: "Something mysterious"
                )
                
                // Start timers
                startDisplayTimer(friendId)
                startCooldownTimer(friendId)
                
                showToast("Caught them ${status.friendlyName ?: "using an app"}!")
                
            } else {
                Log.d(TAG, "Status unavailable after 5 retries")
                // Status unavailable
                updateFriend(friendId) { 
                    it.copy(
                        isPeeping = false, 
                        status = UserStatus(friendId, friendlyName = "Offline or ignoring you"),
                        displayTimer = 5,
                        cooldownSeconds = 5 // Short cooldown for failed peep
                    ) 
                }
                startDisplayTimer(friendId)
                startCooldownTimer(friendId)
                showToast("They might be offline.")
            }
        }
    }

    private fun startCooldownTimer(friendId: String) {
        cooldownJobs[friendId]?.cancel()
        cooldownJobs[friendId] = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _state.value.friends.find { it.profile.id == friendId } ?: break
                if (current.cooldownSeconds <= 0) break
                updateFriend(friendId) { it.copy(cooldownSeconds = it.cooldownSeconds - 1) }
            }
        }
    }

    private fun startDisplayTimer(friendId: String) {
        displayJobs[friendId]?.cancel()
        displayJobs[friendId] = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _state.value.friends.find { it.profile.id == friendId } ?: break
                if (current.displayTimer <= 1) {
                    // Timer done, clear status
                    updateFriend(friendId) { it.copy(displayTimer = 0, status = null) }
                    break
                }
                updateFriend(friendId) { it.copy(displayTimer = it.displayTimer - 1) }
            }
        }
    }

    private fun updateFriend(friendId: String, update: (FriendStatusUi) -> FriendStatusUi) {
        val currentFriends = _state.value.friends
        val newFriends = currentFriends.map { 
            if (it.profile.id == friendId) update(it) else it 
        }
        _state.value = _state.value.copy(friends = newFriends)
    }

    fun hideToast() {
        _state.value = _state.value.copy(toastMessage = null)
    }

    private fun showToast(message: String) {
        _state.value = _state.value.copy(toastMessage = message)
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
