package com.example.csievent.data.remote.dto.event

data class EventResponseDto(
    val id: Long,
    val title: String,
    val description: String,
    val eventDate: String, // LocalDate comes as String in JSON
    val createdBy: Long,
    val maxTeamSize: Int,
    val scoringLocked: Boolean
)
