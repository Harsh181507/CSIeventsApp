package com.example.csievent.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.local.TokenManager
import com.example.csievent.domain.repository.AuthRepository
import com.example.csievent.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state


    fun login(email: String, password: String) {

        if (_state.value.isLoading) return

        if (email.isBlank() || password.isBlank()) {
            _state.value = LoginState(error = "Enter your email and password")
            return
        }

        viewModelScope.launch {

            _state.value = LoginState(isLoading = true)

            val result = repository.login(email, password)

            result.fold(
                onSuccess = { response ->

                    if (Routes.dashboardFor(response.role) == null) {
                        _state.value = LoginState(
                            error = "This account (${response.role}) can't use the app yet. Contact the organizers."
                        )
                        return@fold
                    }

                    tokenManager.saveToken(response.token, response.role)
                    tokenManager.saveProfile(response.name, response.email)

                    _state.value = LoginState(
                        token = response.token,
                        role = response.role
                    )
                }
                ,
                onFailure = { throwable ->
                    _state.value = LoginState(
                        error = throwable.message ?: "Login failed"
                    )
                }
            )
        }
    }

}
