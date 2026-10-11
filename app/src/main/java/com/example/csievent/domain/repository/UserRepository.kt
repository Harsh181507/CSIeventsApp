package com.example.csievent.domain.repository

import com.example.csievent.data.remote.dto.AuthResponseDto
import com.example.csievent.data.remote.dto.user.UserResponseDto


interface UserRepository {

    suspend fun getAllJudges(): Result<List<UserResponseDto>>


    suspend fun getAllUsers(): Result<List<UserResponseDto>>


    suspend fun updateUserRole(userId: Long, role: String): Result<String>

    suspend fun getMe(): Result<UserResponseDto>

    suspend fun updateMyName(name: String): Result<UserResponseDto>

    /** Returns the new session; other devices are signed out. */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<AuthResponseDto>

    suspend fun deleteMyAccount(password: String): Result<String>
}