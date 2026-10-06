package com.example.csievent.data.remote.interceptor

import com.example.csievent.data.local.AuthStateManager
import com.example.csievent.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp application interceptor responsible for two things:
 *
 * 1. **Attach JWT token** — reads the token from [TokenManager] and adds it
 *    as an `Authorization: Bearer <token>` header on every outgoing request.
 *
 * 2. **Detect 401 Unauthorized** — if the server responds with HTTP 401
 *    (expired or invalid token), clears the token from DataStore and notifies
 *    [AuthStateManager] so that [MainActivity] can redirect to the Login screen.
 *    Login/register calls are excluded: a 401 there just means a wrong password.
 *
 * [TokenManager.currentTokenBlocking] keeps the token in memory, so only the
 * very first request reads DataStore.
 *
 * Reference — OkHttp interceptors:
 * https://square.github.io/okhttp/features/interceptors/
 *
 * File: app/src/main/java/com/example/csievent/data/remote/interceptor/AuthInterceptor.kt
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager:    TokenManager,
    private val authStateManager: AuthStateManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val isAuthCall = request.url.encodedPath.startsWith("/auth/")

        // ------------------------------------------------------------------
        // Step 1: Build the outgoing request with the Authorization header
        // ------------------------------------------------------------------
        val requestBuilder = request.newBuilder()

        val token = if (isAuthCall) null else tokenManager.currentTokenBlocking()
        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        // ------------------------------------------------------------------
        // Step 2: Execute the request
        // ------------------------------------------------------------------
        val response = chain.proceed(requestBuilder.build())

        // ------------------------------------------------------------------
        // Step 3: If a logged-in request returns 401, the session is over:
        //         clear the token and send the user back to Login
        // ------------------------------------------------------------------
        if (response.code == HTTP_UNAUTHORIZED && !isAuthCall && !token.isNullOrEmpty()) {
            runBlocking {
                tokenManager.clear()   // wipe the expired token from DataStore
            }
            authStateManager.notifyUnauthorized()  // tell the UI layer
        }

        return response
    }

    companion object {
        private const val HTTP_UNAUTHORIZED = 401
    }
}
