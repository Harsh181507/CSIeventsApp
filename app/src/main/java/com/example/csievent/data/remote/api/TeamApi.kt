package com.example.csievent.data.remote.api

import com.example.csievent.data.remote.dto.team.TeamResponseDto
import okhttp3.ResponseBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface TeamApi {

    @GET("teams/event/{eventId}")
    suspend fun getTeamsByEvent(
        @Path("eventId") eventId: Long
    ): List<TeamResponseDto>

    // Teams the logged-in judge should score in this event
    @GET("judge/events/{eventId}/teams")
    suspend fun getJudgeTeams(
        @Path("eventId") eventId: Long
    ): List<TeamResponseDto>


    // Every team the logged-in student is in, across events
    @GET("teams/my")
    suspend fun getMyTeams(): List<TeamResponseDto>


    @GET("teams/event/{eventId}/my")
    suspend fun getMyTeam(
        @Path("eventId") eventId: Long
    ): TeamResponseDto


    @POST("teams/{eventId}")
    suspend fun createTeam(
        @Path("eventId") eventId: Long,
        @Query("teamName") teamName: String
    ): TeamResponseDto


    @POST("teams/join-by-code")
    suspend fun joinTeamByCode(
        @Query("code") code: String
    ): TeamResponseDto

    @POST("teams/join/{teamId}")
    suspend fun joinTeam(
        @Path("teamId") teamId: Long
    ): ResponseBody

    @DELETE("teams/{teamId}/leave")
    suspend fun leaveTeam(
        @Path("teamId") teamId: Long
    ): ResponseBody
}
