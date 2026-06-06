package com.anonymous.peep.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anonymous.peep.data.model.Profile
import com.anonymous.peep.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: Profile? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val app: Application,
    private val authRepository: AuthRepository,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val profile = authRepository.fetchProfile()
            _state.value = _state.value.copy(
                isLoading = false,
                profile = profile,
            )
        }
    }
}
