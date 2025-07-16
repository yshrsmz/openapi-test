package com.codingfeline.openapikotlin.runtime.auth

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.security.MessageDigest
import java.util.Base64

class OAuth2ClientTest {
    
    @Test
    fun `should generate valid PKCE code verifier`() {
        val verifier = OAuth2Client.generateCodeVerifier()
        
        // Verify length is between 43-128 characters (as per RFC 7636)
        assertThat(verifier.length).isAtLeast(43)
        assertThat(verifier.length).isAtMost(128)
        
        // Verify it only contains allowed characters
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        assertThat(verifier.all { it in allowedChars }).isTrue()
    }
    
    @Test
    fun `should generate valid PKCE code challenge from verifier`() {
        val verifier = "test-verifier-123"
        val challenge = OAuth2Client.generateCodeChallenge(verifier)
        
        // Manually compute expected challenge
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        val expected = Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
        
        assertThat(challenge).isEqualTo(expected)
    }
    
    @Test
    fun `should build correct authorization URL with PKCE`() {
        val config = OAuth2Config(
            authorizationEndpoint = "https://auth.example.com/oauth/authorize",
            tokenEndpoint = "https://auth.example.com/oauth/token",
            clientId = "test-client-id",
            redirectUri = "app://callback",
            scope = "read write"
        )
        
        val (url, state, verifier) = OAuth2Client.buildAuthorizationUrl(config)
        
        assertThat(url).contains("https://auth.example.com/oauth/authorize")
        assertThat(url).contains("client_id=test-client-id")
        assertThat(url).contains("redirect_uri=app%3A%2F%2Fcallback")
        assertThat(url).contains("response_type=code")
        assertThat(url).contains("scope=read+write")
        assertThat(url).contains("code_challenge_method=S256")
        assertThat(url).contains("code_challenge=")
        assertThat(url).contains("state=$state")
        
        assertThat(state).isNotEmpty()
        assertThat(verifier).isNotEmpty()
    }
    
    @Test
    fun `should exchange authorization code for token`() = runTest {
        val config = OAuth2Config(
            authorizationEndpoint = "https://auth.example.com/oauth/authorize",
            tokenEndpoint = "https://auth.example.com/oauth/token",
            clientId = "test-client-id",
            redirectUri = "app://callback",
            clientSecret = "test-secret"
        )
        
        val mockTokenResponse = """
            {
                "access_token": "test-access-token",
                "token_type": "Bearer",
                "expires_in": 3600,
                "refresh_token": "test-refresh-token",
                "scope": "read write"
            }
        """.trimIndent()
        
        // This test would need a mock HTTP client
        // For now, we'll test the token response parsing
        val token = OAuth2Client.parseTokenResponse(mockTokenResponse)
        
        assertThat(token.accessToken).isEqualTo("test-access-token")
        assertThat(token.tokenType).isEqualTo("Bearer")
        assertThat(token.expiresIn).isEqualTo(3600)
        assertThat(token.refreshToken).isEqualTo("test-refresh-token")
        assertThat(token.scope).isEqualTo("read write")
    }
    
    @Test
    fun `should create HTTP client with OAuth2 support`() = runTest {
        val tokenManager = TokenManager.InMemory()
        val config = OAuth2Config(
            authorizationEndpoint = "https://auth.example.com/oauth/authorize",
            tokenEndpoint = "https://auth.example.com/oauth/token",
            clientId = "test-client-id",
            redirectUri = "app://callback"
        )
        
        val client = OAuth2Client.create(
            tokenEndpoint = config.tokenEndpoint,
            clientId = config.clientId,
            redirectUri = config.redirectUri,
            tokenManager = tokenManager,
            baseUrl = "https://api.example.com"
        )
        
        assertThat(client).isNotNull()
    }
}