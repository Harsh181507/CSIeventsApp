package com.example.csievent.domain.repository

import com.example.csievent.data.remote.dto.score.LeaderboardResponseDto
import com.example.csievent.data.remote.dto.score.ScoreResponseDto


interface ScoreRepository {

    suspend fun submitScore(
        teamId:     Long,
        criteriaId: Long,
        scoreValue: Int
    ): Result<String>


    /** Saves all criteria scores for one team in one request (criteriaId to score). */
    suspend fun submitScores(teamId: Long, scores: Map<Long, Int>): Result<String>

    suspend fun getScoresByJudge(eventId: Long? = null): Result<List<ScoreResponseDto>>

    /** Live standings (organizer / judge). */
    suspend fun getLeaderboard(eventId: Long): Result<List<LeaderboardResponseDto>>

    /** Final results for everyone, once scoring is locked. */
    suspend fun getPublicLeaderboard(eventId: Long): Result<List<LeaderboardResponseDto>>
}