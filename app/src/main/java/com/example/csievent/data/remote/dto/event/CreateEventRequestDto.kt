package com.example.csievent.data.remote.dto.event

/** Body for creating and for editing an event. */
data class CreateEventRequestDto(
    val title: String,
    val description: String?,
    val eventDate: String, // yyyy-MM-dd
    val eventTime: String?, // HH:mm
    val venue: String?,
    val maxTeamSize: Int
)
