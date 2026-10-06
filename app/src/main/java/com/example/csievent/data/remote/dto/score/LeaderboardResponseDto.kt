package com.example.csievent.data.remote.dto.score

data class LeaderboardResponseDto(
    val teamId: Long,
    val teamName: String,
    /** Average total per judge (rounded to 2 decimals). */
    val totalScore: Double,
    val judgeCount: Long = 0,
    val rank: Int
)
