package com.example.csievent.data.repository

import com.example.csievent.data.remote.api.EventApi
import com.example.csievent.data.remote.dto.event.CreateEventRequestDto
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.data.remote.dto.event.JudgeEventResponseDto
import com.example.csievent.domain.repository.EventRepository
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventApi: EventApi
) : EventRepository {

    override suspend fun getAllEvents(): Result<List<EventResponseDto>> {
        return try {
            val response = eventApi.getAllEvents()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createEvent(
        request: CreateEventRequestDto
    ): Result<EventResponseDto> {
        return try {
            val response = eventApi.createEvent(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun lockScoring(eventId: Long): Result<String> {
        return try {
            val response = eventApi.lockScoring(eventId)
            Result.success(response.string())   // ← convert ResponseBody to String
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun registerForEvent(eventId: Long): Result<String> {
        return try {
            Result.success(eventApi.registerForEvent(eventId).string())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getJudgeEvents(): Result<List<JudgeEventResponseDto>> {
        return try {
            val response = eventApi.getJudgeEvents()
            Result.success(response)
        }catch (e:Exception){
            Result.failure(e)
        }
    }


}
