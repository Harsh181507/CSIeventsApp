package com.example.csievent.data.remote.api

import com.example.csievent.data.remote.dto.score.LeaderboardResponseDto
import com.example.csievent.data.remote.dto.score.ScoreResponseDto
import com.example.csievent.data.remote.dto.score.SubmitScoreRequestDto
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ScoreApi {


    @POST("scores")
    suspend fun submitScore(
        @Body request: SubmitScoreRequestDto
    ): ResponseBody


    @GET("scores/judge")
    suspend fun getScoresByJudge(): List<ScoreResponseDto>


    @GET("scores/event/{eventId}/summary")
    suspend fun getLeaderboard(
        @Path("eventId") eventId: Long
    ): List<LeaderboardResponseDto>
}