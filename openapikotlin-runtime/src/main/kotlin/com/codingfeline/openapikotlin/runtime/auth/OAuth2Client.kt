package com.codingfeline.openapikotlin.runtime.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

/**
 * OAuth2 configuration
 */
data class OAuth2Config(
    val authorizationEndpoint: String,
    val tokenEndpoint: String,
    val clientId: String,
    val redirectUri: String,
    val clientSecret: String? = null,
    val scope: String? = null,
    val tokenManager: TokenManager? = null
)

/**
 * OAuth2 token response
 */
@Serializable
internal data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Long? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    val scope: String? = null
)

/**
 * OAuth2 client factory with PKCE support
 */
object OAuth2Client {
    
    private const val VERIFIER_LENGTH = 64
    private val VERIFIER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
    
    /**
     * Creates an HTTP client with OAuth2 authentication
     */
    fun create(
        tokenEndpoint: String,
        clientId: String,
        redirectUri: String,
        tokenManager: TokenManager,
        baseUrl: String,
        clientSecret: String? = null,
        engine: HttpClientEngine = CIO.create()
    ): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            
            install(AuthInterceptor) {
                this.tokenManager = tokenManager
                this.onTokenRefresh = { refreshToken ->
                    refreshAccessToken(
                        tokenEndpoint = tokenEndpoint,
                        clientId = clientId,
                        clientSecret = clientSecret,
                        refreshToken = refreshToken
                    )
                }
            }
            
            defaultRequest {
                url(baseUrl)
            }
        }
    }
    
    /**
     * Generates a PKCE code verifier
     */
    fun generateCodeVerifier(): String {
        val random = SecureRandom()
        return (1..VERIFIER_LENGTH)
            .map { VERIFIER_CHARS[random.nextInt(VERIFIER_CHARS.length)] }
            .joinToString("")
    }
    
    /**
     * Generates a PKCE code challenge from verifier using S256 method
     */
    fun generateCodeChallenge(verifier: String): String {
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
    
    /**
     * Builds authorization URL with PKCE parameters
     */
    fun buildAuthorizationUrl(config: OAuth2Config): Triple<String, String, String> {
        val state = generateState()
        val codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier)
        
        val url = URLBuilder(config.authorizationEndpoint).apply {
            parameters.append("client_id", config.clientId)
            parameters.append("redirect_uri", config.redirectUri)
            parameters.append("response_type", "code")
            parameters.append("state", state)
            parameters.append("code_challenge", codeChallenge)
            parameters.append("code_challenge_method", "S256")
            config.scope?.let { parameters.append("scope", it) }
        }.buildString()
        
        return Triple(url, state, codeVerifier)
    }
    
    /**
     * Exchanges authorization code for tokens
     */
    suspend fun exchangeCodeForToken(
        config: OAuth2Config,
        code: String,
        codeVerifier: String
    ): TokenInfo {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        
        return try {
            val response = client.submitForm(
                url = config.tokenEndpoint,
                formParameters = Parameters.build {
                    append("grant_type", "authorization_code")
                    append("code", code)
                    append("redirect_uri", config.redirectUri)
                    append("client_id", config.clientId)
                    append("code_verifier", codeVerifier)
                    config.clientSecret?.let { append("client_secret", it) }
                }
            ).body<TokenResponse>()
            
            TokenInfo(
                accessToken = response.accessToken,
                tokenType = response.tokenType,
                expiresIn = response.expiresIn,
                refreshToken = response.refreshToken,
                scope = response.scope
            )
        } finally {
            client.close()
        }
    }
    
    /**
     * Refreshes access token using refresh token
     */
    suspend fun refreshAccessToken(
        tokenEndpoint: String,
        clientId: String,
        clientSecret: String?,
        refreshToken: String
    ): TokenInfo? {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        
        return try {
            val response = client.submitForm(
                url = tokenEndpoint,
                formParameters = Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", refreshToken)
                    append("client_id", clientId)
                    clientSecret?.let { append("client_secret", it) }
                }
            ).body<TokenResponse>()
            
            TokenInfo(
                accessToken = response.accessToken,
                tokenType = response.tokenType,
                expiresIn = response.expiresIn,
                refreshToken = response.refreshToken ?: refreshToken,
                scope = response.scope
            )
        } catch (e: Exception) {
            null
        } finally {
            client.close()
        }
    }
    
    /**
     * Parses token response JSON (for testing)
     */
    internal fun parseTokenResponse(json: String): TokenInfo {
        val response = Json { ignoreUnknownKeys = true }.decodeFromString<TokenResponse>(json)
        return TokenInfo(
            accessToken = response.accessToken,
            tokenType = response.tokenType,
            expiresIn = response.expiresIn,
            refreshToken = response.refreshToken,
            scope = response.scope
        )
    }
    
    private fun generateState(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}