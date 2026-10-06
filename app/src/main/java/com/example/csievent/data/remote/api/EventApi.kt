package com.example.csievent.data.remote.api

import com.example.csievent.data.remote.dto.event.CreateEventRequestDto
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.data.remote.dto.event.JudgeEventResponseDto
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EventApi {

    @GET("events")
    suspend fun getAllEvents(): List<EventResponseDto>

    @POST("events")
    suspend fun createEvent(
        @Body request: CreateEventRequestDto
    ): EventResponseDto

    @POST("events/{eventId}/lock")
    suspend fun lockScoring(
        @Path("eventId") eventId: Long
    ): ResponseBody

    @POST("events/{eventId}/unlock")
    suspend fun unlockScoring(
        @Path("eventId") eventId: Long
    ): ResponseBody

    @DELETE("events/{eventId}")
    suspend fun deleteEvent(
        @Path("eventId") eventId: Long
    ): ResponseBody

    @POST("events/{eventId}/register")
    suspend fun registerForEvent(
        @Path("eventId") eventId: Long
    ): ResponseBody

    @GET("events/judge")
    suspend fun getJudgeEvents(): List<JudgeEventResponseDto>


}
