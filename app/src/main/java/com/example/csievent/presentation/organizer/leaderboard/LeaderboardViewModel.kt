package com.example.csievent.presentation.organizer.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.domain.repository.ScoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val repository: ScoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LeaderboardState())
    val state: StateFlow<LeaderboardState> = _state

    fun loadLeaderboard(eventId: Long) {

        viewModelScope.launch {

            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            repository.getLeaderboard(eventId)
                .fold(
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
