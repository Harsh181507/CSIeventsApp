package com.example.csievent.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.csievent.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    fun checkAuth(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val token = tokenManager.getToken().first()
            val role = tokenManager.getRole().first()

            if (!token.isNullOrEmpty() && !role.isNullOrEmpty()) {
                onResult(role)
            } else {
                onResult(null)
            }
        }
    }
}
