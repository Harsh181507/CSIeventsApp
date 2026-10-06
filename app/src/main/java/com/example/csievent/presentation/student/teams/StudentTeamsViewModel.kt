package com.example.csievent.presentation.student.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.local.TokenManager
import com.example.csievent.data.remote.ApiException
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.data.remote.dto.team.TeamResponseDto
import com.example.csievent.domain.repository.EventRepository
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


data class StudentTeamsState(
    val isLoading:      Boolean               = false,
    /** A create / join / leave request is running. */
    val isWorking:      Boolean               = false,
    val event:          EventResponseDto?     = null,
    val teams:          List<TeamResponseDto> = emptyList(),
    val myTeam:         TeamResponseDto?      = null,
    val myName:         String                = "",
    val myUserId:       Long?                 = null,
    val error:          String?               = null,
    val message:        String?               = null
)


@HiltViewModel
class StudentTeamsViewModel @Inject constructor(
    private val repository: TeamRepository,
    private val eventRepository: EventRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(StudentTeamsState())
    val state: StateFlow<StudentTeamsState> = _state.asStateFlow()


    fun loadTeams(eventId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Run the calls in parallel
            val eventDeferred  = async { eventRepository.getEvent(eventId) }
            val teamsDeferred  = async { repository.getTeamsByEvent(eventId) }
            val myTeamDeferred = async { repository.getMyTeam(eventId) }
            val name = tokenManager.getName().first().orEmpty()
            val userId = tokenManager.getUserId().first()

            val eventResult  = eventDeferred.await()
            val teamsResult  = teamsDeferred.await()
            val myTeamResult = myTeamDeferred.await()

            // 404 just means "not in a team for this event"
            val myTeamError = myTeamResult.exceptionOrNull() as? ApiException

            _state.update {
                it.copy(
                    isLoading = false,
                    event     = eventResult.getOrNull() ?: it.event,
                    teams     = teamsResult.getOrDefault(it.teams),
                    myTeam    = when {
                        myTeamResult.isSuccess -> myTeamResult.getOrNull()
                        myTeamError?.code == 404 -> null
                        else -> it.myTeam
                    },
                    myName    = name,
                    myUserId  = userId,
                    error     = (eventResult.exceptionOrNull() ?: teamsResult.exceptionOrNull())?.message
                )
            }
        }
    }


    fun createTeam(eventId: Long, teamName: String) {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true) }

            repository.createTeam(eventId, teamName)
                .onSuccess { team ->
                    _state.update { it.copy(isWorking = false, myTeam = team, message = "Team created. Share the code with your teammates.") }
                    loadTeams(eventId)
                }
                .onFailure { error ->
                    _state.update { it.copy(isWorking = false, message = error.message ?: "Failed to create team") }
                }
        }
    }


    fun joinTeamByCode(code: String, eventId: Long) {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true) }

            repository.joinTeamByCode(code)
                .onSuccess { team ->
                    if (team.eventId != eventId) {
                        // The code belongs to a team in another event
                        _state.update { it.copy(isWorking = false, message = "Joined ${team.teamName} (a different event)") }
                    } else {
                        _state.update { it.copy(isWorking = false, myTeam = team, message = "You joined ${team.teamName}") }
                    }
                    loadTeams(eventId)
                }
                .onFailure { error ->
                    _state.update { it.copy(isWorking = false, message = error.message ?: "Invalid code — please try again") }
                }
        }
    }


    fun leaveTeam(teamId: Long, eventId: Long) {
        if (_state.value.isWorking) return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true) }

            repository.leaveTeam(teamId)
                .onSuccess {
                    _state.update { it.copy(isWorking = false, myTeam = null, message = "You left the team") }
                    loadTeams(eventId)
                }
                .onFailure { error ->
                    _state.update { it.copy(isWorking = false, message = error.message ?: "Failed to leave team") }
                }
        }
    }

    fun showMessage(text: String) = _state.update { it.copy(message = text) }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
