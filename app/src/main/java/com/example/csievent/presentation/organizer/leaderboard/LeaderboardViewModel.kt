package com.example.csievent.presentation.organizer.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.local.TokenManager
import com.example.csievent.domain.repository.ScoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val repository: ScoreRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardState())
    val state: StateFlow<LeaderboardState> = _state

    /**
     * Organizers and judges see live standings at any time. Students see the
     * final results, which the server only releases once scoring is locked.
     */
    fun loadLeaderboard(eventId: Long) {

        viewModelScope.launch {

            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            val role = tokenManager.getRole().first()
            val result = if (role == "ORGANIZER" || role == "JUDGE") {
                repository.getLeaderboard(eventId)
            } else {
                repository.getPublicLeaderboard(eventId)
            }

            result.fold(
                onSuccess = { list ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        leaderboard = list
                    )
                },
                onFailure = { throwable ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = throwable.message ?: "Failed to load leaderboard"
                    )
                }
            )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
