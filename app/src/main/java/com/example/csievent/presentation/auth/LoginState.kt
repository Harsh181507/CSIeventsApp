package com.example.csievent.presentation.auth

data class LoginState(
    val isLoading: Boolean = false,
    val token: String? = null,
    val role: String? = null,
    val error: String? = null
)
