package com.example.csievent.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.remote.dto.event.CreateEventRequestDto
import com.example.csievent.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateEventState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEventState())
    val state: StateFlow<CreateEventState> = _state

    fun createEvent(
        title: String,
        description: String,
        eventDate: String,
        maxTeamSize: Int
    ) {
        viewModelScope.launch {

            _state.value = CreateEventState(isLoading = true)

            val request = CreateEventRequestDto(
                title = title,
                description = description,
                eventDate = eventDate,
                maxTeamSize = maxTeamSize
            )

            repository.createEvent(request)
                .onSuccess {
                    _state.value = CreateEventState(success = true)
                }
                .onFailure {
                    _state.value = CreateEventState(
                        error = it.message ?: "Failed to create event"
                    )
                }
        }
    }

    fun clearState() {
        _state.value = CreateEventState()
    }
}
