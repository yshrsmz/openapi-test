package com.example.api

import com.codingfeline.openapikotlin.runtime.auth.TokenInfo
import com.codingfeline.openapikotlin.runtime.auth.TokenManager
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ExampleTest {
    
    @Test
    fun `example of testing generated client code`() = runTest {
        // This test demonstrates how users would test their generated API clients
        val tokenManager = TokenManager.InMemory()
        
        // Save a test token
        val token = TokenInfo(
            accessToken = "test-token",
            tokenType = "Bearer",
            expiresIn = 3600
        )
        tokenManager.saveToken(token)
        
        // In real usage, this would be the generated client
        // val client = OryApiClient.create(
        //     baseUrl = "https://api.example.com",
        //     tokenManager = tokenManager
        // )
        
        // Test that the token manager works
        val retrievedToken = tokenManager.getToken()
        assertThat(retrievedToken).isNotNull()
        assertThat(retrievedToken?.accessToken).isEqualTo("test-token")
    }
    
    @Test
    fun `example of mocking API responses`() = runTest {
        // This shows how users would mock responses for testing
        
        // val mockEngine = MockEngine { request ->
        //     when (request.url.encodedPath) {
        //         "/sessions/whoami" -> {
        //             respond(
        //                 content = """
        //                     {
        //                         "id": "123",
        //                         "email": "test@example.com",
        //                         "email_verified": true,
        //                         "created_at": "2024-01-01T00:00:00Z",
        //                         "updated_at": "2024-01-01T00:00:00Z"
        //                     }
        //                 """.trimIndent(),
        //                 status = HttpStatusCode.OK,
        //                 headers = headersOf(HttpHeaders.ContentType, "application/json")
        //             )
        //         }
        //         else -> error("Unhandled ${request.url.encodedPath}")
        //     }
        // }
        
        // val client = OryApiClient.create(
        //     baseUrl = "https://api.example.com",
        //     engine = mockEngine
        // )
        
        // val identity = client.getCurrentIdentity()
        // assertThat(identity.email).isEqualTo("test@example.com")
    }
}