package com.example.csievent.data.repository

import com.example.csievent.data.remote.api.ScoreApi
import com.example.csievent.data.remote.dto.score.LeaderboardResponseDto
import com.example.csievent.data.remote.dto.score.ScoreResponseDto
import com.example.csievent.data.remote.dto.score.SubmitScoreRequestDto
import com.example.csievent.domain.repository.ScoreRepository
import javax.inject.Inject


class ScoreRepositoryImpl @Inject constructor(
    private val scoreApi: ScoreApi
) : ScoreRepository {

    override suspend fun submitScore(
        teamId:     Long,
        criteriaId: Long,
        scoreValue: Int
    ): Result<String> = runCatching {
        scoreApi.submitScore(
            SubmitScoreRequestDto(
                teamId     = teamId,
                criteriaId = criteriaId,
                scoreValue = scoreValue
            )
        ).string()
    }

    override suspend fun getScoresByJudge(): Result<List<ScoreResponseDto>> = runCatching {
        scoreApi.getScoresByJudge()
    }

    override suspend fun getLeaderboard(
        eventId: Long
    ): Result<List<LeaderboardResponseDto>> = runCatching {
        scoreApi.getLeaderboard(eventId)
    }
}