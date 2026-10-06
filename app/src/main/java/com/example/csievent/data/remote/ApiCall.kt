package com.example.csievent.data.remote

import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Error with a message that can be shown to the user as-is.
 * [code] is the HTTP status, or null for network errors.
 */
class ApiException(message: String, val code: Int? = null) : Exception(message)

/**
 * Runs an API call and turns any failure into an [ApiException] whose message
 * is user-friendly — the backend's own message (e.g. "Team is full") instead
 * of Retrofit's "HTTP 400".
 */
suspend inline fun <T> apiCall(crossinline block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e.toApiException())
    }

fun Throwable.toApiException(): ApiException = when (this) {
    is ApiException -> this
    is HttpException -> {
        val body = try {
            response()?.errorBody()?.string()
        } catch (e: IOException) {
            null
        }
        ApiException(parseServerMessage(body) ?: defaultMessageFor(code()), code())
    }
    is SocketTimeoutException -> ApiException(
        "The server is taking too long to respond. It may be starting up — please try again in a minute."
    )
    is UnknownHostException, is ConnectException -> ApiException(
        "Can't reach the server. Check your internet connection and try again."
    )
    is IOException -> ApiException("Network error. Please try again.")
    else -> ApiException(message ?: "Something went wrong. Please try again.")
}

private fun parseServerMessage(body: String?): String? {
    if (body.isNullOrBlank()) return null
    return try {
        val json = JsonParser.parseString(body)
        if (json.isJsonObject) {
            json.asJsonObject.get("message")?.takeIf { !it.isJsonNull }?.asString
        } else {
            null
        }
    } catch (e: Exception) {
        // Plain-text error bodies (short ones only, never an HTML page)
        body.takeIf { it.length < 200 && !it.trimStart().startsWith("<") }
    }
}

private fun defaultMessageFor(code: Int): String = when (code) {
    400 -> "Invalid request. Please check the details and try again."
    401 -> "Your session has expired. Please log in again."
    403 -> "You don't have permission to do that."
    404 -> "Not found."
    409 -> "This conflicts with existing data. Refresh and try again."
    502, 503, 504 -> "The server is starting up or busy. Please try again in a minute."
    else -> "Something went wrong on the server (error $code). Please try again."
}
