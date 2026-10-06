package com.example.csievent.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.local.ThemeMode
import com.example.csievent.data.local.ThemeSettings
import com.example.csievent.data.local.TokenManager
import com.example.csievent.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val name:       String  = "",
    val email:      String  = "",
    val role:       String  = "",
    val isDeleting: Boolean = false,
    val error:      String? = null,
    /** True once the user logged out or deleted their account. */
    val signedOut:  Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val tokenManager:   TokenManager,
    private val userRepository: UserRepository,
    private val themeSettings:  ThemeSettings
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    val themeMode: StateFlow<ThemeMode> = themeSettings.mode

    init {
        viewModelScope.launch {
            // Show what we have saved immediately, then refresh from the server
            _state.update {
                it.copy(
                    name  = tokenManager.getName().first().orEmpty(),
                    email = tokenManager.getEmail().first().orEmpty(),
                    role  = tokenManager.getRole().first().orEmpty()
                )
            }
            userRepository.getMe().onSuccess { me ->
                tokenManager.saveProfile(me.name, me.email)
                tokenManager.saveUserId(me.id)
                _state.update { it.copy(name = me.name, email = me.email, role = me.role) }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) = themeSettings.setMode(mode)

    fun logout() {
        viewModelScope.launch {
            tokenManager.clear()
            _state.update { it.copy(signedOut = true) }
        }
    }

    fun deleteAccount(password: String) {
        if (_state.value.isDeleting) return

        if (password.isBlank()) {
            _state.update { it.copy(error = "Enter your password to confirm") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, error = null) }

            userRepository.deleteMyAccount(password)
                .onSuccess {
                    tokenManager.clear()
                    _state.update { it.copy(isDeleting = false, signedOut = true) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(isDeleting = false, error = error.message ?: "Could not delete account")
                    }
                }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
