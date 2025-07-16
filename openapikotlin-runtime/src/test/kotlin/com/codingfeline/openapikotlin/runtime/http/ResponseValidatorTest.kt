package com.codingfeline.openapikotlin.runtime.http

import com.google.common.truth.Truth.assertThat
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ResponseValidatorTest {
    
    @Test
    fun `should validate successful response`() = runTest {
        val validator = ResponseValidator()
        
        // Should not throw for 2xx status codes
        validator.validateStatus(HttpStatusCode.OK)
        validator.validateStatus(HttpStatusCode.Created)
        validator.validateStatus(HttpStatusCode.Accepted)
        validator.validateStatus(HttpStatusCode.NoContent)
    }
    
    @Test
    fun `should throw for client error responses`() {
        val validator = ResponseValidator()
        
        assertThrows<ApiException> {
            validator.validateStatus(HttpStatusCode.BadRequest)
        }
        
        assertThrows<ApiException> {
            validator.validateStatus(HttpStatusCode.Unauthorized)
        }
        
        assertThrows<ApiException> {
            validator.validateStatus(HttpStatusCode.Forbidden)
        }
        
        assertThrows<ApiException> {
            validator.validateStatus(HttpStatusCode.NotFound)
        }
    }
    
    @Test
    fun `should throw for server error responses`() {
        val validator = ResponseValidator()
        
        assertThrows<ApiException> {
            validator.validateStatus(HttpStatusCode.InternalServerError)
        }
        
        assertThrows<ApiException> {
            validator.validateStatus(HttpStatusCode.BadGateway)
        }
        
        assertThrows<ApiException> {
            validator.validateStatus(HttpStatusCode.ServiceUnavailable)
        }
    }
    
    @Test
    fun `should validate content type`() = runTest {
        val validator = ResponseValidator()
        
        // Should not throw for JSON content
        validator.validateContentType(ContentType.Application.Json)
        validator.validateContentType(ContentType.parse("application/json; charset=utf-8"))
        
        // Should throw for non-JSON content when expecting JSON
        assertThrows<ApiException> {
            validator.validateContentType(ContentType.Text.Html, expectedType = ContentType.Application.Json)
        }
    }
    
    @Test
    fun `should create ApiException with proper details`() {
        val validator = ResponseValidator()
        
        val exception = assertThrows<ApiException> {
            validator.validateStatus(HttpStatusCode.BadRequest, responseBody = """{"error": "Invalid input"}""")
        }
        
        assertThat(exception.statusCode).isEqualTo(HttpStatusCode.BadRequest)
        assertThat(exception.body).isEqualTo("""{"error": "Invalid input"}""")
        assertThat(exception.isClientError()).isTrue()
    }
}