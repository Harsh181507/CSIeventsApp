package com.example.csievent.data.remote.dto.event

data class EventResponseDto(
    val id: Long,
    val title: String,
    val description: String?,
    val eventDate: String?, // LocalDate comes as String in JSON
    val createdBy: Long?,   // null for events whose creator deleted their account
    val maxTeamSize: Int,
    val scoringLocked: Boolean
)
