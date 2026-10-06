package com.example.csievent.domain.repository

import com.example.csievent.data.remote.dto.team.TeamResponseDto

/**
 * Domain repository interface for all team operations.
 *
 * File: app/src/main/java/com/example/csievent/domain/repository/TeamRepository.kt
 */
interface TeamRepository {

    /** Returns all teams for a given event. */
    suspend fun getTeamsByEvent(eventId: Long): Result<List<TeamResponseDto>>

    /** Teams the logged-in judge should score in an event. */
    suspend fun getJudgeTeams(eventId: Long): Result<List<TeamResponseDto>>

    /**
     * Returns the team the current student belongs to for a given event.
     * Returns failure if the student is not in any team.
     */
    suspend fun getMyTeam(eventId: Long): Result<TeamResponseDto>

    /**
     * Creates a new team. Returns the full response including join code.
     */
    suspend fun createTeam(eventId: Long, teamName: String): Result<TeamResponseDto>

    /**
     * Joins a team using an 8-character join code.
     * Returns the team the student just joined.
     */
    suspend fun joinTeamByCode(code: String): Result<TeamResponseDto>

    /** Joins a team by its database ID. */
    suspend fun joinTeam(teamId: Long): Result<String>

    /** Leaves a team. Only non-leaders can leave. */
    suspend fun leaveTeam(teamId: Long): Result<String>
}