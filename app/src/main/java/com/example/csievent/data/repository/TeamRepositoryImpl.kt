package com.example.csievent.data.repository

import com.example.csievent.data.remote.api.TeamApi
import com.example.csievent.data.remote.apiCall
import com.example.csievent.data.remote.dto.team.TeamResponseDto
import com.example.csievent.domain.repository.TeamRepository
import javax.inject.Inject


class TeamRepositoryImpl @Inject constructor(
    private val teamApi: TeamApi
) : TeamRepository {

    override suspend fun getTeamsByEvent(
        eventId: Long
    ): Result<List<TeamResponseDto>> = apiCall {
        teamApi.getTeamsByEvent(eventId)
    }

    override suspend fun getJudgeTeams(
        eventId: Long
    ): Result<List<TeamResponseDto>> = apiCall {
        teamApi.getJudgeTeams(eventId)
    }


    override suspend fun getMyTeam(
        eventId: Long
    ): Result<TeamResponseDto> = apiCall {
        teamApi.getMyTeam(eventId)
    }


    override suspend fun createTeam(
        eventId: Long,
        teamName: String
    ): Result<TeamResponseDto> = apiCall {
        teamApi.createTeam(eventId, teamName.trim())
    }


    override suspend fun joinTeamByCode(
        code: String
    ): Result<TeamResponseDto> = apiCall {
        teamApi.joinTeamByCode(code.trim())
    }

    override suspend fun joinTeam(
        teamId: Long
    ): Result<String> = apiCall {
        teamApi.joinTeam(teamId).string()
    }

    override suspend fun leaveTeam(
        teamId: Long
    ): Result<String> = apiCall {
        teamApi.leaveTeam(teamId).string()
    }
}
