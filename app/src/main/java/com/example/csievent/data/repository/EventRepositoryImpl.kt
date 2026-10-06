package com.example.csievent.data.repository

import com.example.csievent.data.remote.api.EventApi
import com.example.csievent.data.remote.apiCall
import com.example.csievent.data.remote.dto.event.CreateEventRequestDto
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.data.remote.dto.event.JudgeEventResponseDto
import com.example.csievent.domain.repository.EventRepository
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventApi: EventApi
) : EventRepository {

    override suspend fun getAllEvents(): Result<List<EventResponseDto>> = apiCall {
        eventApi.getAllEvents()
    }

    override suspend fun createEvent(
        request: CreateEventRequestDto
    ): Result<EventResponseDto> = apiCall {
        eventApi.createEvent(request)
    }

    override suspend fun lockScoring(eventId: Long): Result<String> = apiCall {
        eventApi.lockScoring(eventId).string()   // ← convert ResponseBody to String
    }

    override suspend fun unlockScoring(eventId: Long): Result<String> = apiCall {
        eventApi.unlockScoring(eventId).string()
    }

    override suspend fun deleteEvent(eventId: Long): Result<String> = apiCall {
        eventApi.deleteEvent(eventId).string()
    }

    override suspend fun registerForEvent(eventId: Long): Result<String> = apiCall {
        eventApi.registerForEvent(eventId).string()
    }

    override suspend fun getJudgeEvents(): Result<List<JudgeEventResponseDto>> = apiCall {
        eventApi.getJudgeEvents()
    }
}
