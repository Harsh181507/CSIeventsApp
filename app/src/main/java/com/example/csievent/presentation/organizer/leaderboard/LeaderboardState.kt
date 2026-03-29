package com.example.csievent.presentation.organizer.leaderboard

import com.example.csievent.data.remote.dto.score.LeaderboardResponseDto

data class LeaderboardState(
    val isLoading: Boolean = false,
    val leaderboard: List<LeaderboardResponseDto> = emptyList(),
    val error: String? = null
)
