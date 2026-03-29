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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


data class JudgeDashboardState(
    val isLoading: Boolean                   = false,
    val events:    List<JudgeEventResponseDto> = emptyList(),
    val error:     String?                   = null
)



@HiltViewModel
class JudgeDashboardViewModel @Inject constructor(
    private val repository:   EventRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(JudgeDashboardState())
    val state: StateFlow<JudgeDashboardState> = _state.asStateFlow()


    fun loadJudgeEvents() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // FIX: was getAllEvents() — now correctly getJudgeEvents()
            repository.getJudgeEvents()
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
                            error     = error.message ?: "Failed to load your events"
                        )
                    }
                }
        }
    }


    suspend fun logout() {
        tokenManager.clear()
    }
}