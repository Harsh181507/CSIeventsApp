package com.example.csievent.data.repository

import com.example.csievent.data.remote.api.CriteriaApi
import com.example.csievent.data.remote.apiCall
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
    ): Result<CriteriaResponseDto> = apiCall {
        criteriaApi.createCriteria(CreateCriteriaRequestDto(eventId, title, maxScore))
    }

    override suspend fun getCriteriaByEvent(
        eventId: Long
    ): Result<List<CriteriaResponseDto>> = apiCall {
        criteriaApi.getCriteriaByEvent(eventId)
    }

    override suspend fun deleteCriteria(criteriaId: Long): Result<Unit> = apiCall {
        criteriaApi.deleteCriteria(criteriaId).close()
    }
}
