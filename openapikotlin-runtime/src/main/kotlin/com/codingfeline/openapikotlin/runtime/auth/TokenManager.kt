package com.codingfeline.openapikotlin.runtime.auth

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.seconds

/**
 * Token information holder for OAuth2 authentication
 */
data class TokenInfo(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long? = null,
    val refreshToken: String? = null,
    val scope: String? = null,
    val issuedAt: Instant = Clock.System.now()
) {
    /**
     * Checks if the token is expired with optional buffer time
     * @param bufferSeconds Number of seconds before actual expiration to consider token expired
     */
    fun isExpired(bufferSeconds: Int = 0): Boolean {
        val expiresIn = this.expiresIn ?: return false
        val expirationTime = issuedAt + expiresIn.seconds
        val bufferTime = bufferSeconds.seconds
        return Clock.System.now() >= (expirationTime - bufferTime)
    }
}

/**
 * Interface for managing OAuth2 tokens
 */
interface TokenManager {
    /**
     * Saves a token
     */
    suspend fun saveToken(token: TokenInfo)
    
    /**
     * Retrieves the stored token
     */
    suspend fun getToken(): TokenInfo?
    
    /**
     * Clears the stored token
     */
    suspend fun clearToken()
    
    /**
     * In-memory implementation of TokenManager
     */
    class InMemory : TokenManager {
        private var token: TokenInfo? = null
        
        override suspend fun saveToken(token: TokenInfo) {
            this.token = token
        }
        
        override suspend fun getToken(): TokenInfo? = token
        
        override suspend fun clearToken() {
            token = null
        }
    }
}