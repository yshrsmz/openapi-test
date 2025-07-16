package com.codingfeline.openapikotlin.runtime.auth

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*

/**
 * Ktor client plugin for automatic OAuth2 token management
 */
class AuthInterceptor private constructor(
    private val config: Config
) {
    
    class Config {
        lateinit var tokenManager: TokenManager
        var onTokenRefresh: (suspend (String) -> TokenInfo?)? = null
    }
    
    companion object : HttpClientPlugin<Config, AuthInterceptor> {
        override val key: AttributeKey<AuthInterceptor> = AttributeKey("AuthInterceptor")
        
        override fun prepare(block: Config.() -> Unit): AuthInterceptor {
            val config = Config().apply(block)
            return AuthInterceptor(config)
        }
        
        override fun install(plugin: AuthInterceptor, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                val token = plugin.config.tokenManager.getToken()
                
                if (token != null) {
                    // Check if token is expired and refresh if needed
                    if (token.isExpired(bufferSeconds = 30) && token.refreshToken != null) {
                        plugin.config.onTokenRefresh?.let { refreshCallback ->
                            val newToken = refreshCallback(token.refreshToken)
                            if (newToken != null) {
                                plugin.config.tokenManager.saveToken(newToken)
                                context.headers.append(HttpHeaders.Authorization, "${newToken.tokenType} ${newToken.accessToken}")
                            }
                        }
                    } else {
                        // Token is valid, add to headers
                        context.headers.append(HttpHeaders.Authorization, "${token.tokenType} ${token.accessToken}")
                    }
                }
            }
        }
    }
}
