package com.example.csievent.presentation.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.local.TokenManager
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrganizerHomeState(
    val isLoading: Boolean = false,
    val name: String = "",
    val events: List<EventResponseDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class OrganizerHomeViewModel @Inject constructor(
    private val repository: EventRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(OrganizerHomeState())
    val state: StateFlow<OrganizerHomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(name = tokenManager.getName().first().orEmpty()) }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.getAllEvents()
                .onSuccess { events -> _state.update { it.copy(isLoading = false, events = events) } }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message ?: "Failed to load events") }
                }
        }
    }
}
