package com.example.csievent.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.local.TokenManager
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

data class StudentHomeState(
    val isLoading: Boolean = false,
    val name: String = "",
    val events: List<EventResponseDto> = emptyList(),
    /** The student's team per event id. */
    val myTeams: Map<Long, TeamResponseDto> = emptyMap(),
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class StudentHomeViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val teamRepository: TeamRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(StudentHomeState())
    val state: StateFlow<StudentHomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(name = tokenManager.getName().first().orEmpty()) }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val eventsDeferred = async { eventRepository.getAllEvents() }
            val teamsDeferred = async { teamRepository.getMyTeams() }
            val eventsResult = eventsDeferred.await()
            val teamsResult = teamsDeferred.await()

            _state.update { s ->
                s.copy(
                    isLoading = false,
                    events = eventsResult.getOrDefault(s.events),
                    myTeams = teamsResult.getOrNull()?.associateBy { it.eventId } ?: s.myTeams,
                    error = eventsResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun showMessage(text: String) = _state.update { it.copy(message = text) }

    fun clearMessage() = _state.update { it.copy(message = null) }
}
