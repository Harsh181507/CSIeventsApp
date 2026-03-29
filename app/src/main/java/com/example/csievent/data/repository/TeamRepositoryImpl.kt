package com.example.csievent.data.repository

import com.example.csievent.data.remote.api.TeamApi
import com.example.csievent.data.remote.dto.team.TeamResponseDto
import com.example.csievent.domain.repository.TeamRepository
import javax.inject.Inject


class TeamRepositoryImpl @Inject constructor(
    private val teamApi: TeamApi
) : TeamRepository {

    override suspend fun getTeamsByEvent(
        eventId: Long
    ): Result<List<TeamResponseDto>> = runCatching {
        teamApi.getTeamsByEvent(eventId)
    }


    override suspend fun getMyTeam(
        eventId: Long
    ): Result<TeamResponseDto> = runCatching {
        teamApi.getMyTeam(eventId)
    }


    override suspend fun createTeam(
        eventId: Long,
        teamName: String
    ): Result<TeamResponseDto> = runCatching {
        teamApi.createTeam(eventId, teamName)
    }


    override suspend fun joinTeamByCode(
        code: String
    ): Result<TeamResponseDto> = runCatching {
        teamApi.joinTeamByCode(code)
    }

    override suspend fun joinTeam(
        teamId: Long
    ): Result<String> = runCatching {
        teamApi.joinTeam(teamId).string()
    }

    override suspend fun leaveTeam(
        teamId: Long
    ): Result<String> = runCatching {
        teamApi.leaveTeam(teamId).string()
    }
}