package com.example.csievent.data.remote.api

import com.example.csievent.data.remote.dto.event.CreateEventRequestDto
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.data.remote.dto.event.JudgeEventResponseDto
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EventApi {

    @GET("events")
    suspend fun getAllEvents(): List<EventResponseDto>

    @POST("events")
    suspend fun createEvent(
        @Body request: CreateEventRequestDto
    ): EventResponseDto

    @POST("events/{eventId}/lock")
    suspend fun lockScoring(
        @retrofit2.http.Path("eventId") eventId: Long
    ): ResponseBody

    @POST("events/{eventId}/register")
    suspend fun registerForEvent(
        @retrofit2.http.Path("eventId") eventId: Long
    ): ResponseBody

    @GET("events/judge")
    suspend fun getJudgeEvents(): List<JudgeEventResponseDto>


}
