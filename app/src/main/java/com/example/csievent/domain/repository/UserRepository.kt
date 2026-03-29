package com.example.csievent.domain.repository

import com.example.csievent.data.remote.dto.user.UserResponseDto


interface UserRepository {

    suspend fun getAllJudges(): Result<List<UserResponseDto>>


    suspend fun getAllUsers(): Result<List<UserResponseDto>>


    suspend fun updateUserRole(userId: Long, role: String): Result<String>
}