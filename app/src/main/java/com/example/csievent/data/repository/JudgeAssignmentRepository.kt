package com.example.csievent.data.repository

import com.example.csievent.data.remote.api.JudgeAssignmentApi
import com.example.csievent.data.remote.dto.judge.AssignJudgeRequestDto
import com.example.csievent.domain.repository.JudgeAssignmentRepository
import javax.inject.Inject

class JudgeAssignmentRepositoryImpl @Inject constructor(
    private val api: JudgeAssignmentApi
) : JudgeAssignmentRepository {

    override suspend fun assignJudge(
        eventId: Long,
        judgeId: Long,
        teamId: Long?
    ): Result<String> {
        return try {
            Result.success(api.assignJudge(AssignJudgeRequestDto(eventId, judgeId, teamId)).string())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
