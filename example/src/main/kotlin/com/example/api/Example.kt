package com.example.api

import com.codingfeline.openapikotlin.runtime.auth.OAuth2Client
import com.codingfeline.openapikotlin.runtime.auth.OAuth2Config
import com.codingfeline.openapikotlin.runtime.auth.TokenManager
import com.example.api.ory.client.OryApiClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * Example usage of the generated Ory API client
 */
object Example {
    
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            // Create HTTP client with JSON support
            val httpClient = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                    })
                }
            }
            
            // Create token manager for OAuth2
            val tokenManager = TokenManager.InMemory()
            
            // Create OAuth2 client if needed
            val oauth2Config = OAuth2Config(
                clientId = "your-client-id",
                clientSecret = "your-client-secret",
                authorizationEndpoint = "https://auth.example.com/oauth2/auth",
                tokenEndpoint = "https://auth.example.com/oauth2/token",
                redirectUri = "http://localhost:8080/callback"
            )
            
            // OAuth2Client is a companion object, not a class
            // Use it to create an OAuth2-enabled HTTP client
            val oauth2HttpClient = OAuth2Client.create(
                config = oauth2Config,
                httpClient = httpClient,
                tokenManager = tokenManager
            )
            
            // Create the generated API client
            val apiClient = OryApiClient(
                httpClient = httpClient,
                baseUrl = "https://playground.projects.oryapis.com"
            )
            
            try {
                // Example: Call an API endpoint (this will fail without proper auth)
                println("Making API call...")
                // val result = apiClient.someOperation()
                // println("Result: $result")
                
                println("Example completed successfully!")
                println("Note: To actually make API calls, you need:")
                println("1. Valid OAuth2 credentials")
                println("2. Proper authentication setup")
                println("3. Access to the actual Ory API")
            } catch (e: Exception) {
                println("Error: ${e.message}")
                e.printStackTrace()
            } finally {
                httpClient.close()
            }
        }
    }
    
    /**
     * Example of using the generated models
     */
    fun demonstrateModels() {
        // The generated models can be used like this:
        // val identity = Identity(
        //     id = "123",
        //     schemaId = "default",
        //     schemaUrl = "https://example.com/schema",
        //     traits = mapOf("email" to "user@example.com")
        // )
        //
        // // Serialize to JSON
        // val json = Json.encodeToString(Identity.serializer(), identity)
        // println("Serialized: $json")
        //
        // // Deserialize from JSON
        // val decoded = Json.decodeFromString(Identity.serializer(), json)
        // println("Deserialized: $decoded")
    }
}