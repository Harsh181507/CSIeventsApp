package com.example.csievent.data.remote.interceptor

import com.example.csievent.data.local.AuthStateManager
import com.example.csievent.data.local.TokenManager
import kotlinx.coroutines.flow.first
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
 *
 * Why [runBlocking] here?
 * OkHttp interceptors are synchronous — they run on a thread pool and cannot
 * directly call suspend functions. [runBlocking] creates a coroutine scope on
 * the current thread for the DataStore read. The DataStore read is very fast
 * (it reads from an in-memory cache after the first access) so this does NOT
 * cause ANRs. This is the standard approach documented in the OkHttp and
 * Android DataStore guides.
 *
 * Reference — OkHttp interceptors:
 * https://square.github.io/okhttp/features/interceptors/
 *
 * Reference — Android DataStore with OkHttp:
 * https://developer.android.com/topic/libraries/architecture/datastore#synchronous
 *
 * File: app/src/main/java/com/example/csievent/data/remote/interceptor/AuthInterceptor.kt
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager:    TokenManager,
    private val authStateManager: AuthStateManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        // ------------------------------------------------------------------
        // Step 1: Read the stored JWT token (synchronous DataStore read)
        // ------------------------------------------------------------------
        val token = runBlocking {
            tokenManager.getToken().first()
        }

        // ------------------------------------------------------------------
        // Step 2: Build the outgoing request with the Authorization header
        // ------------------------------------------------------------------
        val requestBuilder = chain.request().newBuilder()

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        // ------------------------------------------------------------------
        // Step 3: Execute the request
        // ------------------------------------------------------------------
        val response = chain.proceed(requestBuilder.build())

        // ------------------------------------------------------------------
        // Step 4: If the server returns 401, clear the token and broadcast
        //         a logout event so MainActivity can navigate to Login
        // ------------------------------------------------------------------
        if (response.code == HTTP_UNAUTHORIZED) {
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