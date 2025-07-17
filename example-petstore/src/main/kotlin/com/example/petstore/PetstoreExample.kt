package com.example.petstore

import com.example.petstore.client.PetstoreApiClient
import com.example.petstore.models.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

fun main() = runBlocking {
    println("Petstore API Example")
    println("===================")
    
    // Create HTTP client with JSON support and logging
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    
    // Create API client
    val apiClient = PetstoreApiClient(
        httpClient = httpClient,
        baseUrl = "https://petstore3.swagger.io/api/v3"
    )
    
    try {
        // Example 1: Find pets by status
        println("\n1. Finding available pets...")
        val availablePets = apiClient.findPetsByStatus(status = "available")
        println("Found ${availablePets.size} available pets")
        availablePets.take(3).forEach { pet ->
            println("  - ${pet.name} (ID: ${pet.id})")
        }
        
        // Example 2: Get a specific pet
        println("\n2. Getting pet with ID 1...")
        try {
            val pet = apiClient.getPetById(petid = 1)
            println("Pet details:")
            println("  Name: ${pet.name}")
            println("  Status: ${pet.status}")
            println("  Category: ${pet.category?.name ?: "No category"}")
            println("  Tags: ${pet.tags?.joinToString { it.name ?: "unnamed" } ?: "No tags"}")
        } catch (e: Exception) {
            println("  Pet not found or error: ${e.message}")
        }
        
        // Example 3: Get store inventory
        println("\n3. Getting store inventory...")
        val inventory = apiClient.getInventory()
        println("Store inventory:")
        inventory.forEach { (status, count) ->
            println("  $status: $count pets")
        }
        
        // Example 4: User operations
        println("\n4. User operations...")
        
        // Create a new user
        val newUser = User(
            id = 12345,
            username = "testuser",
            firstName = "Test",
            lastName = "User",
            email = "test@example.com",
            password = "password123",
            phone = "123-456-7890",
            userStatus = 1
        )
        
        println("Creating user '${newUser.username}'...")
        apiClient.createUser(newUser)
        println("User created successfully!")
        
        // Get the user we just created
        println("Fetching user '${newUser.username}'...")
        val fetchedUser = apiClient.getUserByName(username = newUser.username!!)
        println("User details:")
        println("  Username: ${fetchedUser.username}")
        println("  Email: ${fetchedUser.email}")
        println("  Name: ${fetchedUser.firstName} ${fetchedUser.lastName}")
        
        // Example 5: Create a pet (requires authentication)
        println("\n5. Creating a new pet (this will fail due to auth)...")
        val newPet = Pet(
            name = "Fluffy",
            photoUrls = listOf("https://example.com/fluffy.jpg"),
            status = PetStatus.AVAILABLE,
            category = Category(id = 1, name = "Dogs"),
            tags = listOf(Tag(id = 1, name = "friendly"))
        )
        
        try {
            val createdPet = apiClient.addPet(newPet)
            println("Created pet with ID: ${createdPet.id}")
        } catch (e: Exception) {
            println("Failed to create pet (expected - requires authentication): ${e.message}")
        }
        
    } catch (e: Exception) {
        println("Error: ${e.message}")
        e.printStackTrace()
    } finally {
        httpClient.close()
    }
    
    println("\nExample completed!")
}