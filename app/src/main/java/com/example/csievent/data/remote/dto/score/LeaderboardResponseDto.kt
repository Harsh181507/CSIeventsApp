package com.example.csievent.data.remote.dto.score

data class LeaderboardResponseDto(
    val teamId: Long,
    val teamName: String,
    val totalScore: Long,
    val rank: Int
)
