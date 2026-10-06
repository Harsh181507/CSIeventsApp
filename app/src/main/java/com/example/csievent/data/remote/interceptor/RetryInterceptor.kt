package com.example.csievent.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Retries GET requests that fail with 502/503/504, which the server returns
 * for a moment while it is starting up or when it is briefly overloaded.
 * Only GETs are retried, because repeating them can never change data.
 */
class RetryInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)

        if (request.method != "GET") return response

        var attempt = 0
        while (response.code in RETRYABLE_CODES && attempt < MAX_RETRIES) {
            attempt++
            response.close()
            try {
                Thread.sleep(BACKOFF_MS * attempt)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
            response = chain.proceed(request)
        }

        return response
    }

    private companion object {
        val RETRYABLE_CODES = setOf(502, 503, 504)
        const val MAX_RETRIES = 2
        const val BACKOFF_MS = 1500L
    }
}
