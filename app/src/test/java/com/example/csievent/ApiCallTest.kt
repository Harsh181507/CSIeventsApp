package com.example.csievent

import com.example.csievent.data.remote.ApiException
import com.example.csievent.data.remote.apiCall
import com.example.csievent.data.remote.dto.event.EventResponseDto
import com.example.csievent.data.remote.dto.score.LeaderboardResponseDto
import com.example.csievent.data.remote.toApiException
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiCallTest {

    private fun httpError(code: Int, body: String, type: String = "application/json") =
        HttpException(Response.error<Any>(code, body.toResponseBody(type.toMediaType())))

    @Test
    fun `server JSON message is shown to the user`() {
        val e = httpError(400, """{"success":false,"message":"Team is full — maximum 4 members allowed","data":null}""")
            .toApiException()
        assertEquals("Team is full — maximum 4 members allowed", e.message)
        assertEquals(400, e.code)
    }

    @Test
    fun `plain text error body is used when short`() {
        assertEquals("Judge not assigned", httpError(403, "Judge not assigned", "text/plain").toApiException().message)
    }

    @Test
    fun `html error page falls back to a friendly message`() {
        val e = httpError(502, "<html><body>Bad Gateway</body></html>", "text/html").toApiException()
        assertTrue(e.message!!.contains("starting up"))
    }

    @Test
    fun `network failures get readable messages`() {
        assertTrue(SocketTimeoutException().toApiException().message!!.contains("taking too long"))
        assertTrue(UnknownHostException().toApiException().message!!.contains("internet"))
    }

    @Test
    fun `apiCall wraps failures and passes results through`() = runBlocking {
        val ok = apiCall { 42 }
        assertEquals(42, ok.getOrNull())

        val failed = apiCall<Int> { throw httpError(409, """{"message":"Email already registered"}""") }
        val error = failed.exceptionOrNull()
        assertTrue(error is ApiException)
        assertEquals("Email already registered", error!!.message)
    }

    @Test
    fun `dto parsing matches backend json`() {
        val gson = Gson()

        val entry = gson.fromJson(
            """{"teamId":5,"teamName":"Alpha","totalScore":18.5,"judgeCount":2,"rank":1}""",
            LeaderboardResponseDto::class.java
        )
        assertEquals(18.5, entry.totalScore, 0.0001)
        assertEquals(2L, entry.judgeCount)

        val event = gson.fromJson(
            """{"id":1,"title":"Hack","description":null,"eventDate":"2030-01-01","createdBy":null,"maxTeamSize":4,"scoringLocked":false}""",
            EventResponseDto::class.java
        )
        assertNull(event.description)
        assertNull(event.createdBy)
    }
}
