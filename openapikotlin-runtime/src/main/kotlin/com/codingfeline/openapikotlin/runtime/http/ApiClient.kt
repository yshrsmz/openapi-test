package com.codingfeline.openapikotlin.runtime.http

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Base HTTP client configuration for OpenAPI generated clients
 */
object ApiClient {
    
    /**
     * Creates a configured HTTP client
     */
    fun create(
        baseUrl: String,
        engine: HttpClientEngine = CIO.create(),
        requestTimeoutMs: Long? = 30000,
        connectTimeoutMs: Long? = 10000,
        headers: Map<String, String> = emptyMap(),
        enableLogging: Boolean = false
    ): HttpClient {
        return HttpClient(engine) {
            // Install JSON content negotiation
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }
            
            // Configure timeouts
            install(HttpTimeout) {
                requestTimeoutMs?.let { requestTimeoutMillis = it }
                connectTimeoutMs?.let { connectTimeoutMillis = it }
            }
            
            // Configure logging if enabled
            if (enableLogging) {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.INFO
                }
            }
            
            // Configure default request
            defaultRequest {
                url(baseUrl)
                
                // Set default headers
                header(HttpHeaders.Accept, ContentType.Application.Json)
                header(HttpHeaders.UserAgent, "OpenAPIKotlin/1.0")
                
                // Add custom headers
                headers.forEach { (key, value) ->
                    header(key, value)
                }
            }
            
            // Install response validation
            HttpResponseValidator {
                validateResponse { response ->
                    if (!response.status.isSuccess()) {
                        throw ResponseException(response, "API request failed")
                    }
                }
            }
        }
    }
}