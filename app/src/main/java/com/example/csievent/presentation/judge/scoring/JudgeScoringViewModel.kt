package com.example.csievent.presentation.judge.scoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.remote.dto.criteria.CriteriaResponseDto
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.data.remote.dto.score.ScoreResponseDto
import com.example.csievent.data.remote.dto.team.TeamResponseDto
import com.example.csievent.domain.repository.CriteriaRepository
import com.example.csievent.domain.repository.EventRepository
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

data class JudgeScoringState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val event: EventResponseDto? = null,
    val teams: List<TeamResponseDto> = emptyList(),
    val criteria: List<CriteriaResponseDto> = emptyList(),
    /** All scores this judge gave in the event. */
    val scores: List<ScoreResponseDto> = emptyList(),
    val currentTeamId: Long? = null,
    /** Values being edited for the current team: criteriaId -> score (null = not set). */
    val draft: Map<Long, Int?> = emptyMap(),
    /** The draft differs from what is saved. */
    val dirty: Boolean = false,
    val error: String? = null,
    val message: String? = null
) {
    val currentTeam: TeamResponseDto? get() = teams.firstOrNull { it.id == currentTeamId }

    /** Teams with a score for every criterion. */
    val scoredTeamIds: Set<Long>
        get() = if (criteria.isEmpty()) emptySet() else
            scores.groupBy { it.teamId }
                .filterValues { list -> list.map { it.criteriaId }.toSet().size >= criteria.size }
                .keys

    val allSet: Boolean get() = criteria.isNotEmpty() && criteria.all { draft[it.id] != null }
    val total: Int get() = draft.values.sumOf { it ?: 0 }
    val maxTotal: Int get() = criteria.sumOf { it.maxScore }
    val locked: Boolean get() = event?.scoringLocked == true
}

@HiltViewModel
class JudgeScoringViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val teamRepository: TeamRepository,
    private val criteriaRepository: CriteriaRepository,
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(JudgeScoringState())
    val state: StateFlow<JudgeScoringState> = _state.asStateFlow()

    private var loadedEventId: Long? = null

    fun load(eventId: Long, teamId: Long) {
        if (loadedEventId == eventId) return
        loadedEventId = eventId

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val eventD = async { eventRepository.getEvent(eventId) }
            val teamsD = async { teamRepository.getJudgeTeams(eventId) }
            val criteriaD = async { criteriaRepository.getCriteriaByEvent(eventId) }
            val scoresD = async { scoreRepository.getScoresByJudge(eventId) }

            val teamsResult = teamsD.await()
            val criteriaResult = criteriaD.await()
            val scores = scoresD.await().getOrDefault(emptyList())
            val event = eventD.await().getOrNull()

            val failure = teamsResult.exceptionOrNull() ?: criteriaResult.exceptionOrNull()
            if (failure != null) {
                loadedEventId = null
                _state.update { it.copy(isLoading = false, error = failure.message) }
                return@launch
            }

            val criteria = criteriaResult.getOrDefault(emptyList())
            _state.update {
                it.copy(
                    isLoading = false,
                    event = event,
                    teams = teamsResult.getOrDefault(emptyList()),
                    criteria = criteria,
                    scores = scores,
                    currentTeamId = teamId,
                    draft = draftFor(teamId, criteria, scores),
                    dirty = false
                )
            }
        }
    }

    fun retry(eventId: Long, teamId: Long) {
        loadedEventId = null
        load(eventId, teamId)
    }

    /** Switch to another team; unsaved edits are dropped (the screen asks first). */
    fun selectTeam(teamId: Long) {
        _state.update {
            it.copy(currentTeamId = teamId, draft = draftFor(teamId, it.criteria, it.scores), dirty = false)
        }
    }

    fun setScore(criteriaId: Long, value: Int) {
        val max = _state.value.criteria.firstOrNull { it.id == criteriaId }?.maxScore ?: return
        val clamped = value.coerceIn(0, max)
        _state.update { it.copy(draft = it.draft + (criteriaId to clamped), dirty = true) }
    }

    /** Saves the current team's scores (all at once), then optionally moves on. */
    fun save(goToNext: Boolean) {
        val s = _state.value
        val teamId = s.currentTeamId ?: return
        val eventId = loadedEventId ?: return
        if (s.isSaving || !s.allSet) return

        val values = s.draft.mapNotNull { (criteriaId, value) -> value?.let { criteriaId to it } }.toMap()

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            scoreRepository.submitScores(teamId, values)
                .onSuccess {
                    val scores = scoreRepository.getScoresByJudge(eventId).getOrDefault(s.scores)
                    val teamName = s.currentTeam?.teamName ?: "team"

                    _state.update { it.copy(isSaving = false, scores = scores, dirty = false) }

                    if (goToNext) {
                        val next = nextUnscored(_state.value)
                        if (next != null) {
                            selectTeam(next.id)
                            _state.update { it.copy(message = "Saved $teamName. Next: ${next.teamName}") }
                        } else {
                            _state.update { it.copy(message = "Saved $teamName. All teams are scored.") }
                        }
                    } else {
                        _state.update { it.copy(message = "Scores saved for $teamName") }
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false, message = error.message ?: "Couldn't save scores") }
                }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }

    /** The next team after the current one (wrapping around) that isn't fully scored. */
    private fun nextUnscored(s: JudgeScoringState): TeamResponseDto? {
        val scored = s.scoredTeamIds
        val start = s.teams.indexOfFirst { it.id == s.currentTeamId }
        if (s.teams.isEmpty()) return null
        for (offset in 1..s.teams.size) {
            val team = s.teams[(start + offset).mod(s.teams.size)]
            if (team.id !in scored) return team
        }
        return null
    }

    private fun draftFor(
        teamId: Long,
        criteria: List<CriteriaResponseDto>,
        scores: List<ScoreResponseDto>
    ): Map<Long, Int?> {
        val saved = scores.filter { it.teamId == teamId }.associate { it.criteriaId to it.scoreValue }
        return criteria.associate { it.id to saved[it.id] }
    }
}
