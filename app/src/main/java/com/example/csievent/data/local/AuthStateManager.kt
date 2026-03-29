package com.example.csievent.data.local

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthStateManager @Inject constructor() {

    // Internal mutable flow — only this class can emit
    private val _authEvent = MutableSharedFlow<AuthEvent>(
        replay      = 0,          // no replay — new collectors won't see past events
        extraBufferCapacity = 1   // buffer one event to avoid suspension in interceptor
    )

    val authEvent: SharedFlow<AuthEvent> = _authEvent.asSharedFlow()


    fun notifyUnauthorized() {
        _authEvent.tryEmit(AuthEvent.Unauthorized)
    }
}


sealed class AuthEvent {
    object Unauthorized : AuthEvent()
}