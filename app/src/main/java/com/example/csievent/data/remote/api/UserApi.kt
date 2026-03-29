package com.example.csievent.data.remote.api

import com.example.csievent.data.remote.dto.user.UserResponseDto
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT


interface UserApi {

    @GET("users/judges")
    suspend fun getAllJudges(): List<UserResponseDto>

    @GET("users/all")
    suspend fun getAllUsers(): List<UserResponseDto>


    @PUT("users/role")
    suspend fun updateUserRole(
        @Body request: UpdateUserRoleRequest
    ): ResponseBody
}

data class UpdateUserRoleRequest(
    val userId: Long,
    val role:   String
)