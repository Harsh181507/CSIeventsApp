package com.example.csievent.presentation.organizer.roles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.remote.dto.user.UserResponseDto
import com.example.csievent.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoleManagementState(
    val isLoading:      Boolean               = false,
    val users:          List<UserResponseDto> = emptyList(),
    val error:          String?               = null,
    val successMessage: String?               = null
)


@HiltViewModel
class RoleManagementViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RoleManagementState())
    val state: StateFlow<RoleManagementState> = _state.asStateFlow()


    fun loadUsers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            userRepository.getAllUsers()
                .fold(
                    onSuccess = { users ->
                        _state.update {
                            it.copy(isLoading = false, users = users)
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error     = error.message ?: "Failed to load users"
                            )
                        }
                    }
                )
        }
    }

    // =========================================================================
    // UPDATE ROLE
    // =========================================================================

    /**
     * Updates a user's role and refreshes the list.
     *
     * @param userId the ID of the user to update
     * @param role   the new role — "STUDENT" or "JUDGE"
     */
    fun updateRole(userId: Long, role: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            userRepository.updateUserRole(userId, role)
                .fold(
                    onSuccess = {
                        _state.update {
                            it.copy(
                                successMessage = "Role updated to $role successfully"
                            )
                        }
                        loadUsers() // refresh the list
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error     = error.message ?: "Failed to update role"
                            )
                        }
                    }
                )
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    fun clearMessage() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}