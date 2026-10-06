package com.example.csievent.presentation.judge.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.domain.repository.CriteriaRepository
import com.example.csievent.domain.repository.ScoreRepository
import com.example.csievent.domain.repository.TeamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class JudgeTeamsViewModel @Inject constructor(
    private val repository: TeamRepository,
    private val scoreRepository: ScoreRepository,
    private val criteriaRepository: CriteriaRepository
) : ViewModel() {

    private val _state = MutableStateFlow(JudgeTeamsState())
    val state: StateFlow<JudgeTeamsState> = _state.asStateFlow()


    /**
     * Loads the teams this judge should score (all teams, or only the ones the
     * organizer picked) and marks the ones already fully scored.
     */
    fun loadTeams(eventId: Long) {
        viewModelScope.launch {

            _state.update { it.copy(isLoading = true, error = null) }

            val teamsDeferred    = async { repository.getJudgeTeams(eventId) }
            val scoresDeferred   = async { scoreRepository.getScoresByJudge(eventId) }
            val criteriaDeferred = async { criteriaRepository.getCriteriaByEvent(eventId) }

            val teamsResult   = teamsDeferred.await()
            val scores        = scoresDeferred.await().getOrDefault(emptyList())
            val criteriaCount = criteriaDeferred.await().getOrNull()?.size ?: 0

            val scoredTeamIds = if (criteriaCount == 0) emptySet() else
                scores.groupBy { it.teamId }
                    .filterValues { teamScores -> teamScores.map { it.criteriaId }.toSet().size >= criteriaCount }
                    .keys

            teamsResult.fold(
                onSuccess = { teams ->
                    _state.update {
                        it.copy(
                            isLoading     = false,
                            teams         = teams,
                            scoredTeamIds = scoredTeamIds
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
