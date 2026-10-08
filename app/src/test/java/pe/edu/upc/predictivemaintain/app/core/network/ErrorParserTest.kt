package pe.edu.upc.predictivemaintain.app.core.network

import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import retrofit2.Response

class ErrorParserTest {

    private val errorParser = ErrorParser()

    @Test
    fun `parse RFC 7807 JSON with code and fieldErrors successfully`() {
        val json = """
            {
                "type": "about:blank",
                "title": "Bad Request",
                "status": 400,
                "detail": "Validation failed for request.",
                "instance": "/api/v1/auth/login",
                "code": "VALIDATION_ERROR",
                "unknownKeyToIgnore": "foo_bar",
                "errors": {
                    "email": "Email is required",
                    "password": "Password must be at least 8 characters"
                }
            }
        """.trimIndent()

        val result = errorParser.parse(json, 400)

        assertEquals(400, result.httpStatus)
        assertEquals("VALIDATION_ERROR", result.code)
        assertEquals("Validation failed for request.", result.detail)
        assertEquals(2, result.fieldErrors.size)
        assertEquals("Email is required", result.fieldErrors["email"])
        assertEquals("Password must be at least 8 characters", result.fieldErrors["password"])
    }

    @Test
    fun `parse RFC 7807 JSON without code or errors defaults gracefully`() {
        val json = """
            {
                "status": 403,
                "detail": "Subscription is not active",
                "code": "SUBSCRIPTION_NOT_ACTIVE"
            }
        """.trimIndent()

        val result = errorParser.parse(json, 403)

        assertEquals(403, result.httpStatus)
        assertEquals("SUBSCRIPTION_NOT_ACTIVE", result.code)
        assertEquals("Subscription is not active", result.detail)
        assertTrue(result.fieldErrors.isEmpty())
    }

    @Test
    fun `parse null or blank json returns default HTTP error`() {
        val result = errorParser.parse(null, 500)

        assertEquals(500, result.httpStatus)
        assertEquals("HTTP_500", result.code)
        assertEquals("HTTP Error 500", result.detail)
        assertTrue(result.fieldErrors.isEmpty())
    }

    @Test
    fun `safeApiCall with 401 carrying token returns SessionExpired`() = runTest {
        val requestWithToken = Request.Builder()
            .url("https://example.com/api/v1/users/me")
            .header("Authorization", "Bearer token123")
            .build()

        val responseBody = """{"status":401,"detail":"Unauthorized"}"""
            .toResponseBody("application/json".toMediaType())

        val rawResponse = okhttp3.Response.Builder()
            .request(requestWithToken)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body(responseBody)
            .build()

        val retrofitResponse = Response.error<String>(responseBody, rawResponse)

        val outcome = safeApiCall(errorParser) { retrofitResponse }

        assertTrue(outcome is Outcome.Failure)
        val error = (outcome as Outcome.Failure).error
        assertTrue("Expected SessionExpired error", error is AppError.SessionExpired)
    }

    @Test
    fun `safeApiCall with 401 without token returns AppError Api`() = runTest {
        val requestWithoutToken = Request.Builder()
            .url("https://example.com/api/v1/auth/login")
            .build()

        val responseBody = """{"status":401,"code":"INVALID_CREDENTIALS","detail":"Invalid credentials"}"""
            .toResponseBody("application/json".toMediaType())

        val rawResponse = okhttp3.Response.Builder()
            .request(requestWithoutToken)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body(responseBody)
            .build()

        val retrofitResponse = Response.error<String>(responseBody, rawResponse)

        val outcome = safeApiCall(errorParser) { retrofitResponse }

        assertTrue(outcome is Outcome.Failure)
        val error = (outcome as Outcome.Failure).error
        assertTrue("Expected Api error", error is AppError.Api)
        val apiError = error as AppError.Api
        assertEquals(401, apiError.httpStatus)
        assertEquals("INVALID_CREDENTIALS", apiError.code)
        assertEquals("Invalid credentials", apiError.detail)
    }
}
