package com.example.csievent.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class EventFormState(
    /** Loading the event to edit. */
    val isLoadingEvent: Boolean = false,
    /** The event being edited, once loaded (null when creating). */
    val original: EventResponseDto? = null,
    val loadError: String? = null,
    val isSaving: Boolean = false,
    /** Set once the event is saved, so the screen can move on. */
    val savedEventId: Long? = null,
    val error: String? = null
)

/** Values of the form's fields. [date] is yyyy-MM-dd, [time] is HH:mm. */
data class EventFormInput(
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val venue: String,
    val maxTeamSize: Int
)

/** Creates a new event, or edits one when the screen has an event id. */
@HiltViewModel
class EventFormViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EventFormState())
    val state: StateFlow<EventFormState> = _state.asStateFlow()

    fun loadForEdit(eventId: Long) {
        if (_state.value.original != null || _state.value.isLoadingEvent) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingEvent = true, loadError = null) }
            repository.getEvent(eventId)
                .onSuccess { event -> _state.update { it.copy(isLoadingEvent = false, original = event) } }
                .onFailure { error -> _state.update { it.copy(isLoadingEvent = false, loadError = error.message) } }
        }
    }

    fun save(eventId: Long?, input: EventFormInput) {
        if (_state.value.isSaving) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            val request = CreateEventRequestDto(
                title = input.title.trim(),
                description = input.description.trim().ifBlank { null },
                eventDate = input.date,
                eventTime = input.time,
                venue = input.venue.trim().ifBlank { null },
                maxTeamSize = input.maxTeamSize
            )

            val result = if (eventId == null) repository.createEvent(request) else repository.updateEvent(eventId, request)
            result
                .onSuccess { event -> _state.update { it.copy(isSaving = false, savedEventId = event.id) } }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false, error = error.message ?: "Couldn't save the event") }
                }
        }
    }
}
