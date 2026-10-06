package com.example.csievent.data.repository

import com.example.csievent.data.remote.api.ScoreApi
import com.example.csievent.data.remote.apiCall
import com.example.csievent.data.remote.dto.score.BatchScoreRequestDto
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
    ): Result<String> = apiCall {
        scoreApi.submitScore(
            SubmitScoreRequestDto(
                teamId     = teamId,
                criteriaId = criteriaId,
                scoreValue = scoreValue
            )
        ).string()
    }

    override suspend fun submitScores(
        teamId: Long,
        scores: Map<Long, Int>
    ): Result<String> = apiCall {
        scoreApi.submitScores(
            BatchScoreRequestDto(
                teamId = teamId,
                scores = scores.map { (criteriaId, value) ->
                    BatchScoreRequestDto.Entry(criteriaId, value)
                }
            )
        ).string()
    }

    override suspend fun getScoresByJudge(eventId: Long?): Result<List<ScoreResponseDto>> = apiCall {
        scoreApi.getScoresByJudge(eventId)
    }

    override suspend fun getLeaderboard(
        eventId: Long
    ): Result<List<LeaderboardResponseDto>> = apiCall {
        scoreApi.getLeaderboard(eventId)
    }

    override suspend fun getPublicLeaderboard(
        eventId: Long
    ): Result<List<LeaderboardResponseDto>> = apiCall {
        scoreApi.getPublicLeaderboard(eventId)
    }
}
