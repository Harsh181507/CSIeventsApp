package com.example.csievent.presentation.student.events

import com.example.csievent.data.remote.dto.event.EventResponseDto

data class StudentEventsState(
    val isLoading: Boolean = false,
    val events: List<EventResponseDto> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)
