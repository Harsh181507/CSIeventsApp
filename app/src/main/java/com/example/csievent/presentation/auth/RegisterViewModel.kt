package com.example.csievent.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.local.TokenManager
import com.example.csievent.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state

    fun register(
        name: String,
        email: String,
        password: String
    ) {

        if (_state.value.isLoading) return

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _state.value = RegisterState(error = "Fill in your name, email and password")
            return
        }
        if (password.length < 6) {
            _state.value = RegisterState(error = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {

            _state.value = RegisterState(isLoading = true)

            val result = repository.register(name, email, password)

            result.fold(
                onSuccess = { response ->

                    // 🔥 Save token like Login
                    tokenManager.saveToken(response.token, response.role)
                    tokenManager.saveProfile(response.name, response.email)
                    tokenManager.saveUserId(response.userId)

                    _state.value = RegisterState(
                        token = response.token,
                        role = response.role
                    )
                },
                onFailure = { throwable ->
                    _state.value = RegisterState(
                        error = throwable.message ?: "Registration failed"
                    )
                }
            )
        }
    }
}