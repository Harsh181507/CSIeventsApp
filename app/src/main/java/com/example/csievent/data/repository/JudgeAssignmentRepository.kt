package com.example.csievent.data.repository

import com.example.csievent.data.remote.api.JudgeAssignmentApi
import com.example.csievent.data.remote.apiCall
import com.example.csievent.data.remote.dto.judge.AssignJudgeRequestDto
import com.example.csievent.data.remote.dto.judge.JudgeAssignmentDto
import com.example.csievent.domain.repository.JudgeAssignmentRepository
import javax.inject.Inject

class JudgeAssignmentRepositoryImpl @Inject constructor(
    private val api: JudgeAssignmentApi
) : JudgeAssignmentRepository {

    override suspend fun assignJudge(
        eventId: Long,
        judgeId: Long,
        teamIds: List<Long>
    ): Result<String> = apiCall {
        api.assignJudge(AssignJudgeRequestDto(eventId, judgeId, teamIds)).string()
    }

    override suspend fun getAssignments(eventId: Long): Result<List<JudgeAssignmentDto>> = apiCall {
        api.getAssignments(eventId)
    }

    override suspend fun removeJudge(eventId: Long, judgeId: Long): Result<String> = apiCall {
        api.removeJudge(eventId, judgeId).string()
    }
}
