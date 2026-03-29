package com.example.csievent.data.remote.api


import com.example.csievent.data.remote.dto.AuthResponseDto
import com.example.csievent.data.remote.dto.LoginRequestDto
import com.example.csievent.data.remote.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): AuthResponseDto

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): AuthResponseDto
}
