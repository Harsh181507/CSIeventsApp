package com.example.csievent.presentation.judge.teams

import com.example.csievent.data.remote.dto.team.TeamResponseDto

data class JudgeTeamsState(
    val isLoading: Boolean = false,
    val teams: List<TeamResponseDto> = emptyList(),
    val error: String? = null
)
