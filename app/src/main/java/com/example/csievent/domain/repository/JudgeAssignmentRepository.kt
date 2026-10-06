package com.example.csievent.domain.repository

import com.example.csievent.data.remote.dto.judge.JudgeAssignmentDto

interface JudgeAssignmentRepository {

    /** Sets the teams [judgeId] scores in [eventId]; empty = all teams. */
    suspend fun assignJudge(
        eventId: Long,
        judgeId: Long,
        teamIds: List<Long>
    ): Result<String>

    suspend fun getAssignments(eventId: Long): Result<List<JudgeAssignmentDto>>

    suspend fun removeJudge(eventId: Long, judgeId: Long): Result<String>
}
