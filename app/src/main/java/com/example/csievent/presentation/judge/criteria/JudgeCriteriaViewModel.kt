package com.example.csievent.presentation.judge.criteria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.remote.dto.criteria.CriteriaResponseDto
import com.example.csievent.data.remote.dto.score.ScoreResponseDto
import com.example.csievent.domain.repository.CriteriaRepository
import com.example.csievent.domain.repository.ScoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JudgeCriteriaState(
    val isLoading:      Boolean                   = false,
    val isSubmitting:   Boolean                   = false,
    val criteria:       List<CriteriaResponseDto> = emptyList(),
    val existingScores: List<ScoreResponseDto>    = emptyList(),
    val error:          String?                   = null,
    val successMessage: String?                   = null
)

@HiltViewModel
class JudgeCriteriaViewModel @Inject constructor(
    private val criteriaRepository: CriteriaRepository,
    private val scoreRepository:    ScoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(JudgeCriteriaState())
    val state: StateFlow<JudgeCriteriaState> = _state.asStateFlow()

    fun loadCriteriaAndScores(eventId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val criteriaDeferred = async { criteriaRepository.getCriteriaByEvent(eventId) }
            val scoresDeferred   = async { scoreRepository.getScoresByJudge() }
            val criteriaResult   = criteriaDeferred.await()
            val scoresResult     = scoresDeferred.await()
            _state.update {
                it.copy(
                    isLoading      = false,
                    criteria       = criteriaResult.getOrDefault(emptyList()),
                    existingScores = scoresResult.getOrDefault(emptyList()),
                    error          = criteriaResult.exceptionOrNull()?.message
                )
            }
        }
    }

    /**
     * Submits all scores at once in parallel.
     * scores: Map of criteriaId -> scoreValue
     */
    fun submitAllScores(teamId: Long, scores: Map<Long, Int>, eventId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }

            val results = scores.map { (criteriaId, scoreValue) ->
                async { scoreRepository.submitScore(teamId, criteriaId, scoreValue) }
            }.awaitAll()

            val firstFailure = results.firstOrNull { it.isFailure }
            if (firstFailure != null) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error        = firstFailure.exceptionOrNull()?.message ?: "Failed to submit scores"
                    )
                }
            } else {
                // Reload scores so indicators update
                scoreRepository.getScoresByJudge().onSuccess { updated ->
                    _state.update { it.copy(existingScores = updated) }
                }
                _state.update {
                    it.copy(
                        isSubmitting   = false,
                        successMessage = "All verdicts cast successfully"
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}