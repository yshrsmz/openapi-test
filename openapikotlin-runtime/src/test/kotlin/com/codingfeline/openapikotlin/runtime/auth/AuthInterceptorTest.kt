package com.codingfeline.openapikotlin.runtime.auth

import com.google.common.truth.Truth.assertThat
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AuthInterceptorTest {
    
    @Test
    fun `should add Bearer token to request headers`() = runTest {
        val tokenManager = TokenManager.InMemory()
        val token = TokenInfo(
            accessToken = "test-access-token",
            tokenType = "Bearer"
        )
        tokenManager.saveToken(token)
        
        val mockEngine = MockEngine { request ->
            assertThat(request.headers[HttpHeaders.Authorization]).isEqualTo("Bearer test-access-token")
            respond(
                content = "{}",
                status = HttpStatusCode.OK
            )
        }
        
        val client = HttpClient(mockEngine) {
            install(AuthInterceptor) {
                this.tokenManager = tokenManager
            }
        }
        
        client.get("https://api.example.com/test")
    }
    
    @Test
    fun `should not add token header when no token available`() = runTest {
        val tokenManager = TokenManager.InMemory()
        
        val mockEngine = MockEngine { request ->
            assertThat(request.headers[HttpHeaders.Authorization]).isNull()
            respond(
                content = "{}",
                status = HttpStatusCode.OK
            )
        }
        
        val client = HttpClient(mockEngine) {
            install(AuthInterceptor) {
                this.tokenManager = tokenManager
            }
        }
        
        client.get("https://api.example.com/test")
    }
    
    @Test
    fun `should refresh token when expired`() = runTest {
        val tokenManager = TokenManager.InMemory()
        val expiredToken = TokenInfo(
            accessToken = "expired-token",
            tokenType = "Bearer",
            refreshToken = "refresh-token",
            expiresIn = -1 // Already expired
        )
        tokenManager.saveToken(expiredToken)
        
        var refreshCalled = false
        val mockEngine = MockEngine { request ->
            respond(
                content = "{}",
                status = HttpStatusCode.OK
            )
        }
        
        val client = HttpClient(mockEngine) {
            install(AuthInterceptor) {
                this.tokenManager = tokenManager
                this.onTokenRefresh = { refreshToken ->
                    refreshCalled = true
                    assertThat(refreshToken).isEqualTo("refresh-token")
                    TokenInfo(
                        accessToken = "new-access-token",
                        tokenType = "Bearer",
                        refreshToken = "new-refresh-token",
                        expiresIn = 3600
                    )
                }
            }
        }
        
        client.get("https://api.example.com/test")
        
        assertThat(refreshCalled).isTrue()
        assertThat(tokenManager.getToken()?.accessToken).isEqualTo("new-access-token")
    }
}