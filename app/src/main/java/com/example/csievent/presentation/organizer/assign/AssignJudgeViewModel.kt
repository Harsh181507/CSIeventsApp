package com.example.csievent.presentation.organizer.assign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.remote.dto.judge.JudgeAssignmentDto
import com.example.csievent.data.remote.dto.team.TeamResponseDto
import com.example.csievent.data.remote.dto.user.UserResponseDto
import com.example.csievent.domain.repository.JudgeAssignmentRepository
import com.example.csievent.domain.repository.TeamRepository
import com.example.csievent.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssignJudgeState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val judges: List<UserResponseDto> = emptyList(),
    val teams: List<TeamResponseDto> = emptyList(),
    /** Judges already on this event and the teams they score. */
    val assignments: List<JudgeAssignmentDto> = emptyList(),
    val successMessage: String? = null,
    /** An assign / remove action failed. */
    val error: String? = null,
    /** The screen's data couldn't be loaded. */
    val loadError: String? = null
)

@HiltViewModel
class AssignJudgeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository,
    private val assignmentRepository: JudgeAssignmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AssignJudgeState())
    val state: StateFlow<AssignJudgeState> = _state.asStateFlow()

    fun loadData(eventId: Long) {
        viewModelScope.launch {

            _state.update { it.copy(isLoading = true, loadError = null) }

            val judgesDeferred      = async { userRepository.getAllJudges() }
            val teamsDeferred       = async { teamRepository.getTeamsByEvent(eventId) }
            val assignmentsDeferred = async { assignmentRepository.getAssignments(eventId) }

            val judgesResult      = judgesDeferred.await()
            val teamsResult       = teamsDeferred.await()
            val assignmentsResult = assignmentsDeferred.await()

            _state.update {
                it.copy(
                    isLoading   = false,
                    judges      = judgesResult.getOrDefault(emptyList()),
                    teams       = teamsResult.getOrDefault(emptyList()),
                    assignments = assignmentsResult.getOrDefault(emptyList()),
                    loadError   = (judgesResult.exceptionOrNull()
                        ?: teamsResult.exceptionOrNull()
                        ?: assignmentsResult.exceptionOrNull())?.message
                )
            }
        }
    }

    /**
     * Sets the teams [judgeId] scores in this event. An empty [teamIds] list
     * lets the judge score every team (including teams formed later).
     */
    fun assignJudge(eventId: Long, judgeId: Long, teamIds: List<Long>) {
        if (_state.value.isSaving) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            assignmentRepository.assignJudge(eventId, judgeId, teamIds)
                .onSuccess { message ->
                    refreshAssignments(eventId)
                    _state.update { it.copy(isSaving = false, successMessage = message) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false, error = error.message) }
                }
        }
    }

    fun removeJudge(eventId: Long, judgeId: Long) {
        if (_state.value.isSaving) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            assignmentRepository.removeJudge(eventId, judgeId)
                .onSuccess { message ->
                    refreshAssignments(eventId)
                    _state.update { it.copy(isSaving = false, successMessage = message) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false, error = error.message) }
                }
        }
    }

    private suspend fun refreshAssignments(eventId: Long) {
        assignmentRepository.getAssignments(eventId).onSuccess { list ->
            _state.update { it.copy(assignments = list) }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(successMessage = null, error = null) }
    }
}
