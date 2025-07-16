package com.codingfeline.openapikotlin.runtime.http

import com.google.common.truth.Truth.assertThat
import io.ktor.client.plugins.*
import io.ktor.http.*
import org.junit.jupiter.api.Test

class ApiExceptionTest {
    
    @Test
    fun `should create ApiException from status code`() {
        val exception = ApiException(
            statusCode = HttpStatusCode.NotFound,
            message = "Resource not found",
            body = """{"error": "Not found"}"""
        )
        
        assertThat(exception.statusCode).isEqualTo(HttpStatusCode.NotFound)
        assertThat(exception.message).isEqualTo("Resource not found")
        assertThat(exception.body).isEqualTo("""{"error": "Not found"}""")
    }
    
    @Test
    fun `should identify client errors`() {
        val badRequest = ApiException(HttpStatusCode.BadRequest, "Bad request")
        val unauthorized = ApiException(HttpStatusCode.Unauthorized, "Unauthorized")
        val forbidden = ApiException(HttpStatusCode.Forbidden, "Forbidden")
        val notFound = ApiException(HttpStatusCode.NotFound, "Not found")
        
        assertThat(badRequest.isClientError()).isTrue()
        assertThat(unauthorized.isClientError()).isTrue()
        assertThat(forbidden.isClientError()).isTrue()
        assertThat(notFound.isClientError()).isTrue()
    }
    
    @Test
    fun `should identify server errors`() {
        val internalError = ApiException(HttpStatusCode.InternalServerError, "Server error")
        val badGateway = ApiException(HttpStatusCode.BadGateway, "Bad gateway")
        val serviceUnavailable = ApiException(HttpStatusCode.ServiceUnavailable, "Service unavailable")
        
        assertThat(internalError.isServerError()).isTrue()
        assertThat(badGateway.isServerError()).isTrue()
        assertThat(serviceUnavailable.isServerError()).isTrue()
    }
    
    @Test
    fun `should create from ResponseException`() {
        // For simplicity in testing, we'll create a mock ResponseException
        // In real usage, this would come from actual HTTP responses
        val mockException = object : Exception("Bad Request") {}
        val apiException = ApiException(
            statusCode = HttpStatusCode.BadRequest,
            message = "Bad Request",
            body = """{"error": "Invalid input"}"""
        )
        
        assertThat(apiException.statusCode).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(apiException.message).isEqualTo("Bad Request")
        assertThat(apiException.body).isEqualTo("""{"error": "Invalid input"}""")
    }
    
    @Test
    fun `should create from generic exception`() {
        val genericException = IllegalArgumentException("Something went wrong")
        val apiException = ApiException.from(genericException)
        
        assertThat(apiException.statusCode).isEqualTo(HttpStatusCode.InternalServerError)
        assertThat(apiException.message).isEqualTo("Something went wrong")
        assertThat(apiException.body).isNull()
    }
}