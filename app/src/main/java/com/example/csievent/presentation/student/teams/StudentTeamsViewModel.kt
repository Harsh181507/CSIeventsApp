package com.example.csievent.presentation.student.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.remote.dto.team.TeamResponseDto
import com.example.csievent.domain.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class StudentTeamsState(
    val isLoading:      Boolean               = false,
    val teams:          List<TeamResponseDto> = emptyList(),
    val myTeam:         TeamResponseDto?      = null,
    val error:          String?               = null,
    val successMessage: String?               = null
)


@HiltViewModel
class StudentTeamsViewModel @Inject constructor(
    private val repository: TeamRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StudentTeamsState())
    val state: StateFlow<StudentTeamsState> = _state.asStateFlow()


    fun loadTeams(eventId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Run both API calls in parallel
            val teamsDeferred = async { repository.getTeamsByEvent(eventId) }
            val myTeamDeferred = async { repository.getMyTeam(eventId) }

            val teamsResult  = teamsDeferred.await()
            val myTeamResult = myTeamDeferred.await()

            _state.update {
                it.copy(
                    isLoading = false,
                    teams     = teamsResult.getOrDefault(emptyList()),
                    // myTeam is null if student is not in any team (404 = failure)
                    myTeam    = myTeamResult.getOrNull(),
                    error     = teamsResult.exceptionOrNull()?.message
                )
            }
        }
    }


    fun createTeam(eventId: Long, teamName: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            repository.createTeam(eventId, teamName)
                .fold(
                    onSuccess = { createdTeam ->
                        _state.update {
                            it.copy(
                                isLoading      = false,
                                myTeam         = createdTeam,
                                successMessage = "Team created! Share code: ${createdTeam.joinCode}"
                            )
                        }
                        loadTeams(eventId)
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error     = error.message ?: "Failed to create team"
                            )
                        }
                    }
                )
        }
    }


    fun joinTeamByCode(code: String, eventId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            repository.joinTeamByCode(code)
                .fold(
                    onSuccess = { joinedTeam ->
                        _state.update {
                            it.copy(
                                isLoading      = false,
                                myTeam         = joinedTeam,
                                successMessage = "Joined ${joinedTeam.teamName} 🎉"
                            )
                        }
                        loadTeams(eventId)
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error     = error.message ?: "Invalid code — please try again"
                            )
                        }
                    }
                )
        }
    }


    fun leaveTeam(teamId: Long, eventId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            repository.leaveTeam(teamId)
                .fold(
                    onSuccess = {
                        _state.update {
                            it.copy(
                                isLoading      = false,
                                myTeam         = null,
                                successMessage = "Left team successfully"
                            )
                        }
                        loadTeams(eventId)
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error     = error.message ?: "Failed to leave team"
                            )
                        }
                    }
                )
        }
    }

    fun clearMessage() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}