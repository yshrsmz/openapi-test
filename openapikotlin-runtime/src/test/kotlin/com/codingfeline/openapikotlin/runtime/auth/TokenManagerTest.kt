package com.codingfeline.openapikotlin.runtime.auth

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

class TokenManagerTest {
    
    @Test
    fun `InMemory TokenManager should store and retrieve tokens`() = runTest {
        val tokenManager = TokenManager.InMemory()
        val token = TokenInfo(
            accessToken = "test-access-token",
            tokenType = "Bearer",
            expiresIn = 3600,
            refreshToken = "test-refresh-token",
            scope = "read write",
            issuedAt = Clock.System.now()
        )
        
        tokenManager.saveToken(token)
        val retrieved = tokenManager.getToken()
        
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.accessToken).isEqualTo("test-access-token")
        assertThat(retrieved?.refreshToken).isEqualTo("test-refresh-token")
        assertThat(retrieved?.tokenType).isEqualTo("Bearer")
        assertThat(retrieved?.scope).isEqualTo("read write")
    }
    
    @Test
    fun `InMemory TokenManager should return null when no token is stored`() = runTest {
        val tokenManager = TokenManager.InMemory()
        
        val retrieved = tokenManager.getToken()
        
        assertThat(retrieved).isNull()
    }
    
    @Test
    fun `InMemory TokenManager should clear tokens`() = runTest {
        val tokenManager = TokenManager.InMemory()
        val token = TokenInfo(
            accessToken = "test-access-token",
            tokenType = "Bearer",
            expiresIn = 3600,
            refreshToken = "test-refresh-token",
            issuedAt = Clock.System.now()
        )
        
        tokenManager.saveToken(token)
        tokenManager.clearToken()
        val retrieved = tokenManager.getToken()
        
        assertThat(retrieved).isNull()
    }
    
    @Test
    fun `TokenInfo should correctly determine if token is expired`() {
        val now = Clock.System.now()
        val expiredToken = TokenInfo(
            accessToken = "expired-token",
            tokenType = "Bearer",
            expiresIn = 3600,
            issuedAt = now - 3700.seconds
        )
        val validToken = TokenInfo(
            accessToken = "valid-token",
            tokenType = "Bearer",
            expiresIn = 3600,
            issuedAt = now - 3500.seconds
        )
        
        assertThat(expiredToken.isExpired()).isTrue()
        assertThat(validToken.isExpired()).isFalse()
    }
    
    @Test
    fun `TokenInfo should handle buffer time for expiration check`() {
        val now = Clock.System.now()
        val almostExpiredToken = TokenInfo(
            accessToken = "almost-expired-token",
            tokenType = "Bearer",
            expiresIn = 3600,
            issuedAt = now - 3590.seconds // 10 seconds left
        )
        
        // With 30 second buffer, should be considered expired
        assertThat(almostExpiredToken.isExpired(bufferSeconds = 30)).isTrue()
        // With 5 second buffer, should still be valid
        assertThat(almostExpiredToken.isExpired(bufferSeconds = 5)).isFalse()
    }
}