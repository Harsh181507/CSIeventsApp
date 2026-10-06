package com.example.csievent.data.repository

import com.example.csievent.data.remote.api.UpdateUserRoleRequest
import com.example.csievent.data.remote.api.UserApi
import com.example.csievent.data.remote.apiCall
import com.example.csievent.data.remote.dto.user.DeleteAccountRequestDto
import com.example.csievent.data.remote.dto.user.UserResponseDto
import com.example.csievent.domain.repository.UserRepository
import javax.inject.Inject


class UserRepositoryImpl @Inject constructor(
    private val api: UserApi
) : UserRepository {

    override suspend fun getAllJudges(): Result<List<UserResponseDto>> = apiCall {
        api.getAllJudges()
    }

    override suspend fun getAllUsers(): Result<List<UserResponseDto>> = apiCall {
        api.getAllUsers()
    }

    override suspend fun updateUserRole(userId: Long, role: String): Result<String> = apiCall {
        api.updateUserRole(UpdateUserRoleRequest(userId, role)).string()
    }

    override suspend fun getMe(): Result<UserResponseDto> = apiCall {
        api.getMe()
    }

    override suspend fun deleteMyAccount(password: String): Result<String> = apiCall {
        api.deleteMe(DeleteAccountRequestDto(password)).string()
    }
}
