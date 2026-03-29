package com.example.csievent.data.repository

import com.example.csievent.data.remote.api.CriteriaApi
import com.example.csievent.data.remote.dto.criteria.CreateCriteriaRequestDto
import com.example.csievent.data.remote.dto.criteria.CriteriaResponseDto
import com.example.csievent.domain.repository.CriteriaRepository
import javax.inject.Inject

class CriteriaRepositoryImpl @Inject constructor(
    private val criteriaApi: CriteriaApi
) : CriteriaRepository {

    override suspend fun createCriteria(
        eventId: Long,
        title: String,
        maxScore: Int
    ): Result<CriteriaResponseDto> {
        return try {
            val response = criteriaApi.createCriteria(
                CreateCriteriaRequestDto(eventId, title, maxScore)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCriteriaByEvent(
        eventId: Long
    ): Result<List<CriteriaResponseDto>> {
        return try {
            val response = criteriaApi.getCriteriaByEvent(eventId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
