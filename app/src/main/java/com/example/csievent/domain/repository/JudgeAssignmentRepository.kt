package com.example.csievent.domain.repository

interface JudgeAssignmentRepository {

    suspend fun assignJudge(
        eventId: Long,
        judgeId: Long,
        teamId: Long?
    ): Result<String>
}
