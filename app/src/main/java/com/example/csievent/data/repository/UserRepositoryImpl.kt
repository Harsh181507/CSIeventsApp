package com.example.csievent.data.repository

import com.example.csievent.data.remote.api.UpdateUserRoleRequest
import com.example.csievent.data.remote.api.UserApi
import com.example.csievent.data.remote.dto.user.UserResponseDto
import com.example.csievent.domain.repository.UserRepository
import javax.inject.Inject


class UserRepositoryImpl @Inject constructor(
    private val api: UserApi
) : UserRepository {

    override suspend fun getAllJudges(): Result<List<UserResponseDto>> = runCatching {
        api.getAllJudges()
    }

    override suspend fun getAllUsers(): Result<List<UserResponseDto>> = runCatching {
        api.getAllUsers()
    }

    override suspend fun updateUserRole(userId: Long, role: String): Result<String> = runCatching {
        api.updateUserRole(UpdateUserRoleRequest(userId, role)).string()
    }
}