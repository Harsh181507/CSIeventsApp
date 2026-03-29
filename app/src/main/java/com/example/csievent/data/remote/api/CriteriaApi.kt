package com.example.csievent.data.remote.api

import com.example.csievent.data.remote.dto.criteria.CreateCriteriaRequestDto
import com.example.csievent.data.remote.dto.criteria.CriteriaResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CriteriaApi {

    @POST("criteria")
    suspend fun createCriteria(
        @Body request: CreateCriteriaRequestDto
    ): CriteriaResponseDto


    @GET("criteria/{eventId}")
    suspend fun getCriteriaByEvent(
        @Path("eventId") eventId: Long
    ): List<CriteriaResponseDto>

    @DELETE("criteria/{id}")
    suspend fun deleteCriteria(@Path("id") criteriaId: Long)

}
