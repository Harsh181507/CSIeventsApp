package com.example.csievent.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.local.TokenManager
import com.example.csievent.data.remote.dto.event.CreateEventRequestDto
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject



data class OrganizerEventsState(
    val isLoading:      Boolean            = false,
    val events:         List<EventResponseDto> = emptyList(),
    val error:          String?            = null,
    val successMessage: String?            = null
)


@HiltViewModel
class OrganizerEventsViewModel @Inject constructor(
    private val repository:    EventRepository,
    private val tokenManager:  TokenManager       // ← constructor-injected (correct)
) : ViewModel() {

    private val _state = MutableStateFlow(OrganizerEventsState())
    val state: StateFlow<OrganizerEventsState> = _state.asStateFlow()


    fun fetchEvents() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            repository.getAllEvents()
                .onSuccess { events ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            events    = events,
                            error     = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error     = error.message ?: "Failed to load events"
                        )
                    }
                }
        }
    }


    fun createEvent(
        title:       String,
        description: String?,
        eventDate:   String,
        maxTeamSize: Int
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            repository.createEvent(
                CreateEventRequestDto(
                    title       = title,
                    description = description,
                    eventDate   = eventDate,
                    maxTeamSize = maxTeamSize
                )
            )
                .onSuccess {
                    fetchEvents()   // refresh the list
                    _state.update {
                        it.copy(
                            isLoading      = false,
                            successMessage = "Event created successfully 🎉"
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error     = error.message ?: "Failed to create event"
                        )
                    }
                }
        }
    }


    fun lockScoring(eventId: Long) {
        viewModelScope.launch {
            repository.lockScoring(eventId)
                .onSuccess {
                    fetchEvents()   // refresh to show "Scoring Locked" chip
                    _state.update {
                        it.copy(successMessage = "Scoring locked 🔒")
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to lock scoring")
                    }
                }
        }
    }


    fun unlockScoring(eventId: Long) {
        viewModelScope.launch {
            repository.unlockScoring(eventId)
                .onSuccess {
                    fetchEvents()
                    _state.update {
                        it.copy(successMessage = "Scoring unlocked 🔓")
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to unlock scoring")
                    }
                }
        }
    }


    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
                .onSuccess {
                    _state.update { s ->
                        s.copy(
                            events         = s.events.filterNot { it.id == eventId },
                            successMessage = "Event deleted"
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to delete event")
                    }
                }
        }
    }


    suspend fun logout() {
        tokenManager.clear()
    }


    fun clearMessage() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}