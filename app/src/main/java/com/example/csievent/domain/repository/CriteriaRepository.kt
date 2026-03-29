package com.example.csievent.domain.repository

import com.example.csievent.data.remote.dto.criteria.CriteriaResponseDto

interface CriteriaRepository {

    suspend fun getCriteriaByEvent(
        eventId: Long
    ): Result<List<CriteriaResponseDto>>

    suspend fun createCriteria(
        eventId: Long,
        title: String,
        maxScore: Int
    ): Result<CriteriaResponseDto>
    suspend fun deleteCriteria(criteriaId: Long): Result<Unit>
}
