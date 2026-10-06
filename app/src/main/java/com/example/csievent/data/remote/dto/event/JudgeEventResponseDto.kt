package com.example.csievent.data.remote.dto.event

data class JudgeEventResponseDto(
    val id: Long,
    val title: String,
    val description: String?,
    val eventDate: String? = null,
    val scoringLocked: Boolean
)
