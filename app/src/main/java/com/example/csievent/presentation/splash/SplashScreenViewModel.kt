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
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository
) : ViewModel() {

    // Started as soon as the splash opens so it runs while the logo shows
    private val freshProfile: Deferred<Result<UserResponseDto>?> = viewModelScope.async {
        if (tokenManager.getToken().first().isNullOrEmpty()) null else userRepository.getMe()
    }

    /**
     * The role whose home screen to open, or null to show Login.
     *
     * The role is refreshed from the server because an organizer may have
     * changed it (e.g. student → judge). If the server is slow (asleep) the
     * saved role is used so the app never hangs on the splash screen.
     */
    suspend fun resolveRole(): String? {
        val token = tokenManager.getToken().first()
        val role = tokenManager.getRole().first()

        if (token.isNullOrEmpty() || role.isNullOrEmpty()) return null

        val fresh = withTimeoutOrNull(FRESH_ROLE_WAIT_MS) { freshProfile.await() }
        val me = fresh?.getOrNull()

        return when {
            me != null && Routes.dashboardFor(me.role) != null -> {
                tokenManager.saveRole(me.role)
                tokenManager.saveProfile(me.name, me.email)
                tokenManager.saveUserId(me.id)
                me.role
            }
            // Session expired or account deleted
            (fresh?.exceptionOrNull() as? ApiException)?.code == 401 -> null
            // Offline / server waking up: carry on with the saved role
            else -> role
        }
    }

    private companion object {
        const val FRESH_ROLE_WAIT_MS = 2_000L
    }
}
