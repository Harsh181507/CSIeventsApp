package com.example.csievent.data.remote.dto.judge

/** A judge assigned to an event; [allTeams] means no specific teams were picked. */
data class JudgeAssignmentDto(
    val judgeId: Long,
    val judgeName: String,
    val judgeEmail: String,
    val allTeams: Boolean,
    val teamIds: List<Long> = emptyList(),
    val teamNames: List<String> = emptyList()
)
