package com.example.csievent.presentation.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.local.TokenManager
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.data.remote.dto.score.LeaderboardResponseDto
import com.example.csievent.domain.repository.EventRepository
import com.example.csievent.domain.repository.ScoreRepository
import com.example.csievent.domain.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultsState(
    val isLoading: Boolean = false,
    val event: EventResponseDto? = null,
    val entries: List<LeaderboardResponseDto> = emptyList(),
    /** The viewer's own team (students), highlighted in the list. */
    val myTeamId: Long? = null,
    /** Organizers and judges see live standings before scoring is locked. */
    val canSeeLive: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val scoreRepository: ScoreRepository,
    private val eventRepository: EventRepository,
    private val teamRepository: TeamRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(ResultsState())
    val state: StateFlow<ResultsState> = _state.asStateFlow()

    fun load(eventId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val role = tokenManager.getRole().first()
            val live = role == "ORGANIZER" || role == "JUDGE"

            val eventD = async { eventRepository.getEvent(eventId) }
            val boardD = async {
                if (live) scoreRepository.getLeaderboard(eventId) else scoreRepository.getPublicLeaderboard(eventId)
            }
            val myTeamD = async { if (role == "STUDENT") teamRepository.getMyTeam(eventId).getOrNull() else null }

            val board = boardD.await()
            _state.update {
                it.copy(
                    isLoading = false,
                    event = eventD.await().getOrNull() ?: it.event,
                    entries = board.getOrDefault(emptyList()),
                    myTeamId = myTeamD.await()?.id,
                    canSeeLive = live,
                    error = board.exceptionOrNull()?.message
                )
            }
        }
    }
}
