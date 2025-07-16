package com.codingfeline.openapikotlin.runtime.http

import com.google.common.truth.Truth.assertThat
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ApiClientTest {
    
    @Test
    fun `should create HTTP client with default configuration`() {
        val client = ApiClient.create(
            baseUrl = "https://api.example.com"
        )
        
        assertThat(client).isNotNull()
        client.close()
    }
    
    @Test
    fun `should apply base URL to all requests`() = runTest {
        val mockEngine = MockEngine { request ->
            assertThat(request.url.toString()).startsWith("https://api.example.com")
            respond(
                content = "{}",
                status = HttpStatusCode.OK
            )
        }
        
        val client = ApiClient.create(
            baseUrl = "https://api.example.com",
            engine = mockEngine
        )
        
        client.get("/users")
        client.close()
    }
    
    @Test
    fun `should include default headers`() = runTest {
        val mockEngine = MockEngine { request ->
            assertThat(request.headers[HttpHeaders.UserAgent]).contains("OpenAPIKotlin")
            assertThat(request.headers[HttpHeaders.Accept]).isEqualTo("application/json")
            respond(
                content = "{}",
                status = HttpStatusCode.OK
            )
        }
        
        val client = ApiClient.create(
            baseUrl = "https://api.example.com",
            engine = mockEngine
        )
        
        client.get("/test")
        client.close()
    }
    
    @Test
    fun `should handle timeouts`() {
        val client = ApiClient.create(
            baseUrl = "https://api.example.com",
            requestTimeoutMs = 5000,
            connectTimeoutMs = 3000
        )
        
        assertThat(client).isNotNull()
        client.close()
    }
    
    @Test
    fun `should support custom headers`() = runTest {
        val mockEngine = MockEngine { request ->
            assertThat(request.headers["X-Custom-Header"]).isEqualTo("custom-value")
            respond(
                content = "{}",
                status = HttpStatusCode.OK
            )
        }
        
        val client = ApiClient.create(
            baseUrl = "https://api.example.com",
            engine = mockEngine,
            headers = mapOf("X-Custom-Header" to "custom-value")
        )
        
        client.get("/test")
        client.close()
    }
}