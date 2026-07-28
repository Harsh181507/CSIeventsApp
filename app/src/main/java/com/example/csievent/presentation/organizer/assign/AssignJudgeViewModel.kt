package com.example.csievent.presentation.organizer.assign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.remote.dto.team.TeamResponseDto
import com.example.csievent.data.remote.dto.user.UserResponseDto
import com.example.csievent.domain.repository.JudgeAssignmentRepository
import com.example.csievent.domain.repository.TeamRepository
import com.example.csievent.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssignJudgeState(
    val isLoading: Boolean = false,
    val judges: List<UserResponseDto> = emptyList(),
    val teams: List<TeamResponseDto> = emptyList(),
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class AssignJudgeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val teamRepository: TeamRepository,
    private val assignmentRepository: JudgeAssignmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AssignJudgeState())
    val state: StateFlow<AssignJudgeState> = _state

    fun loadData(eventId: Long) {
        viewModelScope.launch {

            _state.value = _state.value.copy(isLoading = true)

            val judgesResult = userRepository.getAllJudges()
            val teamsResult = teamRepository.getTeamsByEvent(eventId)

            _state.value = AssignJudgeState(
                isLoading = false,
                judges = judgesResult.getOrDefault(emptyList()),
                teams = teamsResult.getOrDefault(emptyList())
            )
        }
    }

    fun assignJudge(eventId: Long, judgeId: Long, teamId: Long?) {
        viewModelScope.launch {

            val result = assignmentRepository.assignJudge(eventId, judgeId, teamId)

            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        successMessage = it
                    )
                },
                onFailure = {
                    _state.value = _state.value.copy(
                        error = it.message
                    )
                }
            )
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(
            successMessage = null,
            error = null
        )
    }
}


//Check 1 2 3
