package com.example.csievent.data.remote.dto.judge

data class AssignJudgeRequestDto(
    val eventId: Long,
    val judgeId: Long,
    val teamId: Long? = null
)
