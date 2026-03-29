package com.example.csievent.data.remote.dto

data class AuthResponseDto(
    val token: String,
    val userId: Long,
    val name: String,
    val email: String,
    val role: String
)
