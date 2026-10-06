package com.example.csievent.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.local.TokenManager
import com.example.csievent.data.remote.ApiException
import com.example.csievent.data.remote.dto.user.UserResponseDto
import com.example.csievent.domain.repository.UserRepository
import com.example.csievent.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository
) : ViewModel() {

    // Started as soon as the splash opens so it runs during the animation
    private val freshProfile: Deferred<Result<UserResponseDto>?> = viewModelScope.async {
        if (tokenManager.getToken().first().isNullOrEmpty()) null else userRepository.getMe()
    }

    /**
     * Calls [onResult] with the role to open, or null to show Login.
     *
     * The role is refreshed from the server because an organizer may have
     * changed it (e.g. student → judge). If the server is slow (asleep) the
     * saved role is used so the app never hangs on the splash screen.
     */
    fun checkAuth(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val token = tokenManager.getToken().first()
            val role = tokenManager.getRole().first()

            if (token.isNullOrEmpty() || role.isNullOrEmpty()) {
                onResult(null)
                return@launch
            }

            val fresh = withTimeoutOrNull(FRESH_ROLE_WAIT_MS) { freshProfile.await() }
            val me = fresh?.getOrNull()

            when {
                me != null && Routes.dashboardFor(me.role) != null -> {
                    tokenManager.saveRole(me.role)
                    tokenManager.saveProfile(me.name, me.email)
                    onResult(me.role)
                }
                // Session expired or account deleted
                (fresh?.exceptionOrNull() as? ApiException)?.code == 401 -> onResult(null)
                // Offline / server waking up: carry on with the saved role
                else -> onResult(role)
            }
        }
    }

    private companion object {
        const val FRESH_ROLE_WAIT_MS = 2_000L
    }
}
