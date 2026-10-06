package com.example.csievent.data.remote.dto.judge

/**
 * Sets which teams a judge scores in an event.
 * An empty [teamIds] list means the judge can score every team.
 */
data class AssignJudgeRequestDto(
    val eventId: Long,
    val judgeId: Long,
    val teamIds: List<Long> = emptyList()
)
