package com.example.csievent.domain.repository

import com.example.csievent.data.remote.dto.AuthResponseDto


interface AuthRepository {

    suspend fun login(
        email: String,
        password: String
    ): Result<AuthResponseDto>

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<AuthResponseDto>
}
