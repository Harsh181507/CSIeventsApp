package com.example.csievent.presentation.judge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.local.TokenManager
import com.example.csievent.data.remote.dto.event.JudgeEventResponseDto
import com.example.csievent.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JudgeHomeState(
    val isLoading: Boolean = false,
    val name: String = "",
    val events: List<JudgeEventResponseDto> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class JudgeHomeViewModel @Inject constructor(
    private val repository: EventRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(JudgeHomeState())
    val state: StateFlow<JudgeHomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(name = tokenManager.getName().first().orEmpty()) }
        }
    }

    /** Events this judge is assigned to. */
    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.getJudgeEvents()
                .onSuccess { events ->
                    _state.update { it.copy(isLoading = false, events = events) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message ?: "Failed to load your events") }
                }
        }
    }
}
