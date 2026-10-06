package com.example.csievent.domain.repository

import com.example.csievent.data.remote.dto.event.CreateEventRequestDto
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.data.remote.dto.event.JudgeEventResponseDto

interface EventRepository {
    suspend fun getAllEvents(): Result<List<EventResponseDto>>
    suspend fun createEvent(request: CreateEventRequestDto): Result<EventResponseDto>
    suspend fun lockScoring(eventId: Long): Result<String>
    suspend fun unlockScoring(eventId: Long): Result<String>
    suspend fun deleteEvent(eventId: Long): Result<String>
    suspend fun registerForEvent(eventId: Long): Result<String>
    suspend fun getJudgeEvents(): Result<List<JudgeEventResponseDto>>
}
