package com.example.csievent.presentation.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.local.TokenManager
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class StudentDashboardState(
    val isLoading: Boolean             = false,
    val events:    List<EventResponseDto> = emptyList(),
    val error:     String?             = null
)


@HiltViewModel
class StudentDashboardViewModel @Inject constructor(
    private val repository:   EventRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(StudentDashboardState())
    val state: StateFlow<StudentDashboardState> = _state.asStateFlow()

    fun fetchEvents() {
        viewModelScope.launch {

            _state.update { it.copy(isLoading = true, error = null) }

            repository.getAllEvents()
                .onSuccess { events ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            events    = events
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


    suspend fun logout() {
        tokenManager.clear()
    }
}