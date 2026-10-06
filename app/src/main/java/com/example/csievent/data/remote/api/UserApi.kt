package com.example.csievent.data.remote.api

import com.example.csievent.data.remote.dto.user.DeleteAccountRequestDto
import com.example.csievent.data.remote.dto.user.UserResponseDto
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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

    // Profile of the logged-in user (fresh role after an organizer changes it)
    @GET("users/me")
    suspend fun getMe(): UserResponseDto

    // Permanently deletes the logged-in user's account
    @POST("users/me/delete")
    suspend fun deleteMe(
        @Body request: DeleteAccountRequestDto
    ): ResponseBody
}

data class UpdateUserRoleRequest(
    val userId: Long,
    val role:   String
)
