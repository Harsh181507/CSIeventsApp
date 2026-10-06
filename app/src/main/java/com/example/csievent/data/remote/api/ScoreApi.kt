package com.example.csievent.data.remote.api

import com.example.csievent.data.remote.dto.score.BatchScoreRequestDto
import com.example.csievent.data.remote.dto.score.LeaderboardResponseDto
import com.example.csievent.data.remote.dto.score.ScoreResponseDto
import com.example.csievent.data.remote.dto.score.SubmitScoreRequestDto
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ScoreApi {


    @POST("scores")
    suspend fun submitScore(
        @Body request: SubmitScoreRequestDto
    ): ResponseBody

    // All criteria for one team in one request (saved all-or-nothing)
    @POST("scores/batch")
    suspend fun submitScores(
        @Body request: BatchScoreRequestDto
    ): ResponseBody


    @GET("scores/judge")
    suspend fun getScoresByJudge(
        @Query("eventId") eventId: Long? = null
    ): List<ScoreResponseDto>


    // Live standings (organizer / judge)
    @GET("scores/event/{eventId}/summary")
    suspend fun getLeaderboard(
        @Path("eventId") eventId: Long
    ): List<LeaderboardResponseDto>

    // Final results for everyone, available after scoring is locked
    @GET("leaderboard/{eventId}")
    suspend fun getPublicLeaderboard(
        @Path("eventId") eventId: Long
    ): List<LeaderboardResponseDto>
}
