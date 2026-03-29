package com.example.csievent.presentation.judge.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.domain.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class JudgeTeamsViewModel @Inject constructor(
    private val repository: TeamRepository
) : ViewModel() {

    private val _state = MutableStateFlow(JudgeTeamsState())
    val state: StateFlow<JudgeTeamsState> = _state.asStateFlow()


    fun loadTeams(eventId: Long) {
        viewModelScope.launch {

            _state.update { it.copy(isLoading = true, error = null) }

            repository.getTeamsByEvent(eventId)
                .fold(
                    onSuccess = { teams ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                teams     = teams
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error     = error.message ?: "Failed to load teams"
                            )
                        }
                    }
                )
        }
    }
}