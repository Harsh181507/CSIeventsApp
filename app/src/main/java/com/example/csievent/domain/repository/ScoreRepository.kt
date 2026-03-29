package com.example.csievent.domain.repository

import com.example.csievent.data.remote.dto.score.LeaderboardResponseDto
import com.example.csievent.data.remote.dto.score.ScoreResponseDto


interface ScoreRepository {

    suspend fun submitScore(
        teamId:     Long,
        criteriaId: Long,
        scoreValue: Int
    ): Result<String>


    suspend fun getScoresByJudge(): Result<List<ScoreResponseDto>>

    suspend fun getLeaderboard(eventId: Long): Result<List<LeaderboardResponseDto>>
}