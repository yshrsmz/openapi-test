package com.example.simple

import com.example.simple.client.SimpleApiClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

suspend fun main() {
    // Create HTTP client with JSON support
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }
    
    // Create API client
    val apiClient = SimpleApiClient(httpClient)
    
    println("Simple API Client Test")
    println("=====================")
    
    try {
        // Test listUsers
        println("\nCalling listUsers()...")
        val users = apiClient.listUsers()
        println("Response: $users")
        
        // Test getUser
        println("\nCalling getUser(\"123\")...")
        val user = apiClient.getUser("123")
        println("Response: $user")
    } catch (e: Exception) {
        println("Error: ${e.message}")
        println("This is expected - we're testing against a mock API URL")
    } finally {
        httpClient.close()
    }
}