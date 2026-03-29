package com.example.csievent.domain.model

data class User(
    val token: String,
    val userId: Long,
    val name: String,
    val email: String,
    val role: String
)
