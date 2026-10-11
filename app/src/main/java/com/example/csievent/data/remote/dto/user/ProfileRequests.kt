package com.example.csievent.data.remote.dto.user

data class UpdateProfileRequestDto(val name: String)

data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)
