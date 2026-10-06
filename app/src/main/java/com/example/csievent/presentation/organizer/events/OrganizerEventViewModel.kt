package com.example.csievent.presentation.organizer.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.remote.dto.criteria.CriteriaResponseDto
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.data.remote.dto.judge.JudgeAssignmentDto
import com.example.csievent.data.remote.dto.team.TeamResponseDto
import com.example.csievent.domain.repository.CriteriaRepository
import com.example.csievent.domain.repository.EventRepository
import com.example.csievent.domain.repository.JudgeAssignmentRepository
import com.example.csievent.domain.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrganizerEventState(
    val isLoading: Boolean = false,
    val isWorking: Boolean = false,
    val event: EventResponseDto? = null,
    val teams: List<TeamResponseDto> = emptyList(),
    val criteria: List<CriteriaResponseDto> = emptyList(),
    val judges: List<JudgeAssignmentDto> = emptyList(),
    val error: String? = null,
    val message: String? = null,
    /** True after the event was deleted, so the screen closes. */
    val deleted: Boolean = false
)

@HiltViewModel
class OrganizerEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val teamRepository: TeamRepository,
    private val criteriaRepository: CriteriaRepository,
    private val assignmentRepository: JudgeAssignmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OrganizerEventState())
    val state: StateFlow<OrganizerEventState> = _state.asStateFlow()

    fun load(eventId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val eventD = async { eventRepository.getEvent(eventId) }
            val teamsD = async { teamRepository.getTeamsByEvent(eventId) }
            val criteriaD = async { criteriaRepository.getCriteriaByEvent(eventId) }
            val judgesD = async { assignmentRepository.getAssignments(eventId) }

            val eventResult = eventD.await()
            val teams = teamsD.await()
            val criteria = criteriaD.await()
            val judges = judgesD.await()

            _state.update {
                it.copy(
                    isLoading = false,
                    event = eventResult.getOrNull() ?: it.event,
                    teams = teams.getOrDefault(it.teams),
                    criteria = criteria.getOrDefault(it.criteria),
                    judges = judges.getOrDefault(it.judges),
                    error = eventResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun setLocked(eventId: Long, locked: Boolean) {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true) }

            val result = if (locked) eventRepository.lockScoring(eventId) else eventRepository.unlockScoring(eventId)
            result
                .onSuccess {
                    _state.update {
                        it.copy(
                            isWorking = false,
                            event = it.event?.copy(scoringLocked = locked),
                            message = if (locked) "Scoring locked. Results are now visible to everyone." else "Scoring reopened"
                        )
                    }
                }
                .onFailure { error -> _state.update { it.copy(isWorking = false, message = error.message) } }
        }
    }

    fun delete(eventId: Long) {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true) }
            eventRepository.deleteEvent(eventId)
                .onSuccess { _state.update { it.copy(isWorking = false, deleted = true) } }
                .onFailure { error -> _state.update { it.copy(isWorking = false, message = error.message) } }
        }
    }

    fun showMessage(text: String) = _state.update { it.copy(message = text) }

    fun clearMessage() = _state.update { it.copy(message = null) }
}
