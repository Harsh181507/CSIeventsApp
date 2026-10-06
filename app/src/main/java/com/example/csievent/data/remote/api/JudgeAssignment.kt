package com.example.csievent.data.remote.api

import com.example.csievent.data.remote.dto.judge.AssignJudgeRequestDto
import com.example.csievent.data.remote.dto.judge.JudgeAssignmentDto
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface JudgeAssignmentApi {

    // FIX: plain text → ResponseBody
    @POST("judge-assignments")
    suspend fun assignJudge(
        @Body request: AssignJudgeRequestDto
    ): ResponseBody

    @GET("judge-assignments/event/{eventId}")
    suspend fun getAssignments(
        @Path("eventId") eventId: Long
    ): List<JudgeAssignmentDto>

    @DELETE("judge-assignments/event/{eventId}/judge/{judgeId}")
    suspend fun removeJudge(
        @Path("eventId") eventId: Long,
        @Path("judgeId") judgeId: Long
    ): ResponseBody
}
