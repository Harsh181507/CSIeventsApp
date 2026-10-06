package com.example.csievent.presentation.judge.teams

import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.data.remote.dto.team.TeamResponseDto


data class JudgeTeamsState(
    val isLoading: Boolean = false,
    val event: EventResponseDto? = null,
    val teams: List<TeamResponseDto> = emptyList(),
    /** Teams this judge has already scored on every criterion. */
    val scoredTeamIds: Set<Long> = emptySet(),
    val criteriaCount: Int = 0,
    val error: String? = null
)
