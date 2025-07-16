package com.codingfeline.openapikotlin.gradle.domain.model

/**
 * Domain model for OpenAPI security scheme
 */
data class SecurityScheme(
    val type: SecuritySchemeType,
    val description: String? = null,
    val name: String? = null, // For apiKey
    val `in`: ApiKeyLocation? = null, // For apiKey
    val scheme: String? = null, // For http
    val bearerFormat: String? = null, // For http with bearer
    val flows: OAuthFlows? = null, // For oauth2
    val openIdConnectUrl: String? = null // For openIdConnect
) {
    /**
     * Checks if this is a bearer token scheme
     */
    fun isBearerToken(): Boolean {
        return type == SecuritySchemeType.HTTP && scheme?.lowercase() == "bearer"
    }
    
    /**
     * Checks if this is an API key scheme
     */
    fun isApiKey(): Boolean {
        return type == SecuritySchemeType.API_KEY
    }
    
    /**
     * Checks if this is an OAuth2 scheme
     */
    fun isOAuth2(): Boolean {
        return type == SecuritySchemeType.OAUTH2
    }
    
    /**
     * Gets the OAuth2 authorization code flow if available
     */
    fun getAuthorizationCodeFlow(): OAuthFlow? {
        return flows?.authorizationCode
    }
}

/**
 * Security scheme types
 */
enum class SecuritySchemeType {
    API_KEY,
    HTTP,
    OAUTH2,
    OPENID_CONNECT
}

/**
 * API key locations
 */
enum class ApiKeyLocation {
    QUERY,
    HEADER,
    COOKIE
}

/**
 * OAuth flows
 */
data class OAuthFlows(
    val implicit: OAuthFlow? = null,
    val password: OAuthFlow? = null,
    val clientCredentials: OAuthFlow? = null,
    val authorizationCode: OAuthFlow? = null
)

/**
 * OAuth flow
 */
data class OAuthFlow(
    val authorizationUrl: String? = null, // For implicit, authorizationCode
    val tokenUrl: String? = null, // For password, clientCredentials, authorizationCode
    val refreshUrl: String? = null,
    val scopes: Map<String, String> = emptyMap()
)