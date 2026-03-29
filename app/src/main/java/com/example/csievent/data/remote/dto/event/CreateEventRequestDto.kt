package com.example.csievent.data.remote.dto.event

data class CreateEventRequestDto(
    val title: String,
    val description: String?,
    val eventDate: String, // yyyy-MM-dd
    val maxTeamSize: Int
)
