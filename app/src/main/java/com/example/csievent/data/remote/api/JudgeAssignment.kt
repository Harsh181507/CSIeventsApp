package com.example.csievent.data.remote.api

import com.example.csievent.data.remote.dto.judge.AssignJudgeRequestDto
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

interface JudgeAssignmentApi {

    // FIX: plain text → ResponseBody
    @POST("judge-assignments")
    suspend fun assignJudge(
        @Body request: AssignJudgeRequestDto
    ): ResponseBody
}
