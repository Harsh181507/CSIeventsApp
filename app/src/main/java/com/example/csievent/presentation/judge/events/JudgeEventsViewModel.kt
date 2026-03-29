package com.example.csievent.presentation.judge.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JudgeEventsState(
    val isLoading: Boolean = false,
    val events: List<EventResponseDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class JudgeEventsViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    private val _state = MutableStateFlow(JudgeEventsState())
    val state: StateFlow<JudgeEventsState> = _state

    fun fetchEvents() {
        viewModelScope.launch {

            _state.value = _state.value.copy(isLoading = true)

            val result = repository.getAllEvents()

            result.onSuccess { list ->
                _state.value = JudgeEventsState(
                    events = list
                )
            }

            result.onFailure { e ->
                _state.value = JudgeEventsState(
                    error = e.message ?: "Failed to load events"
                )
            }
        }
    }
}
