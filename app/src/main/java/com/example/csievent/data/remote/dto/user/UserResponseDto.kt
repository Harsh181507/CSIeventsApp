package com.example.csievent.data.remote.dto.user


data class UserResponseDto(
    val id:    Long,
    val name:  String,
    val email: String,
    val role:  String = "STUDENT"
)