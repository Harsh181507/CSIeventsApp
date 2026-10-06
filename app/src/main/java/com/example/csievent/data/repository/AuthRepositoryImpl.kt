package com.example.csievent.data.repository

import com.example.csievent.data.remote.api.AuthApi
import com.example.csievent.data.remote.apiCall
import com.example.csievent.data.remote.dto.AuthResponseDto
import com.example.csievent.data.remote.dto.LoginRequestDto
import com.example.csievent.data.remote.dto.RegisterRequestDto
import com.example.csievent.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthRepository {

    override suspend fun login(
        email: String,
        password: String
    ): Result<AuthResponseDto> = apiCall {
        authApi.login(LoginRequestDto(email.trim(), password))
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<AuthResponseDto> = apiCall {
        authApi.register(RegisterRequestDto(name.trim(), email.trim(), password))
    }
}
