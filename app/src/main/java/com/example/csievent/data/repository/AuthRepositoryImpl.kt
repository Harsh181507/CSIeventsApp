package com.example.csievent.data.repository

import com.example.csievent.domain.repository.AuthRepository
import com.example.csievent.data.remote.api.AuthApi
import com.example.csievent.data.remote.dto.AuthResponseDto
import com.example.csievent.data.remote.dto.LoginRequestDto
import com.example.csievent.data.remote.dto.RegisterRequestDto
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthRepository {

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthResponseDto> {
        return try {
            val response = authApi.login(
                LoginRequestDto(email, password)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<AuthResponseDto> {
        return try {
            val response = authApi.register(
                RegisterRequestDto(name, email, password)
            )
            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
