package com.example.csievent.presentation.student.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentEventsViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StudentEventsState())
    val state: StateFlow<StudentEventsState> = _state

    init {
        fetchEvents()
    }

    fun fetchEvents() {
        viewModelScope.launch {

            _state.value = _state.value.copy(isLoading = true)

            val result = repository.getAllEvents()

            result.onSuccess { events ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    events = events,
                    error = null
                )
            }

            result.onFailure { throwable ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = throwable.message ?: "Failed to load events"
                )
            }
        }
    }

    fun registerForEvent(eventId: Long) {
        viewModelScope.launch {

            _state.value = _state.value.copy(isLoading = true)

            val result = repository.registerForEvent(eventId)

            result.onSuccess { message ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = message,
                    error = null
                )
            }

            result.onFailure { throwable ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = throwable.message ?: "Registration failed"
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(
            successMessage = null,
            error = null
        )
    }
}
