package com.example.csievent.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.remote.dto.event.CreateEventRequestDto
import com.example.csievent.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class CreateEventState(
    val isLoading: Boolean = false,
    /** Set once the event exists, so the screen can open it. */
    val createdEventId: Long? = null,
    val error: String? = null
)

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEventState())
    val state: StateFlow<CreateEventState> = _state.asStateFlow()

    /** [eventDate] is yyyy-MM-dd. */
    fun createEvent(
        title: String,
        description: String,
        eventDate: String,
        maxTeamSize: Int
    ) {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val request = CreateEventRequestDto(
                title = title.trim(),
                description = description.trim().ifBlank { null },
                eventDate = eventDate,
                maxTeamSize = maxTeamSize
            )

            repository.createEvent(request)
                .onSuccess { event -> _state.update { it.copy(isLoading = false, createdEventId = event.id) } }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message ?: "Failed to create event") }
                }
        }
    }
}
