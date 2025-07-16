package com.codingfeline.openapikotlin.gradle.integration

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PluginIntegrationTest {
    
    @TempDir
    lateinit var testProjectDir: File
    
    @Test
    fun `should generate code using Gradle plugin`() {
        // Create build file
        val buildFile = File(testProjectDir, "build.gradle.kts")
        buildFile.writeText("""
            plugins {
                id("org.jetbrains.kotlin.jvm") version "2.1.21"
                id("com.codingfeline.openapi")
            }
            
            repositories {
                mavenLocal()
                mavenCentral()
            }
            
            dependencies {
                implementation("com.codingfeline.openapikotlin:openapikotlin-runtime:1.0.0-SNAPSHOT")
                implementation("io.ktor:ktor-client-core:2.3.0")
                implementation("io.ktor:ktor-client-cio:2.3.0")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            }
            
            openApiKotlin {
                inputSpec = file("petstore.json")
                outputDir = layout.buildDirectory.file("generated/openapi").get().asFile
                packageName = "com.example.api"
                
                models {
                    generateDataAnnotations = true
                    generateDefaultValues = true
                }
                
                client {
                    clientClassName = "PetStoreClient"
                    generateErrorHandling = true
                }
            }
            
            kotlin {
                sourceSets {
                    main {
                        kotlin {
                            srcDir(layout.buildDirectory.file("generated/openapi"))
                        }
                    }
                }
            }
        """.trimIndent())
        
        // Create settings file
        val settingsFile = File(testProjectDir, "settings.gradle.kts")
        settingsFile.writeText("""
            rootProject.name = "test-project"
            
            pluginManagement {
                repositories {
                    mavenLocal()
                    gradlePluginPortal()
                }
                
                resolutionStrategy {
                    eachPlugin {
                        if (requested.id.id == "com.codingfeline.openapi") {
                            useModule("com.codingfeline.openapikotlin:openapikotlin-gradle-plugin:1.0.0-SNAPSHOT")
                        }
                    }
                }
            }
        """.trimIndent())
        
        // Create OpenAPI spec
        val specFile = File(testProjectDir, "petstore.json")
        specFile.writeText("""
            {
              "openapi": "3.0.0",
              "info": {
                "title": "Pet Store API",
                "version": "1.0.0"
              },
              "paths": {
                "/pets": {
                  "get": {
                    "operationId": "listPets",
                    "parameters": [
                      {
                        "name": "limit",
                        "in": "query",
                        "schema": {
                          "type": "integer"
                        }
                      }
                    ],
                    "responses": {
                      "200": {
                        "description": "Success",
                        "content": {
                          "application/json": {
                            "schema": {
                              "type": "array",
                              "items": {
                                "${'$'}ref": "#/components/schemas/Pet"
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              },
              "components": {
                "schemas": {
                  "Pet": {
                    "type": "object",
                    "required": ["id", "name"],
                    "properties": {
                      "id": {
                        "type": "integer",
                        "format": "int64"
                      },
                      "name": {
                        "type": "string"
                      },
                      "tag": {
                        "type": "string"
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent())
        
        // Run the generateOpenApiCode task
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("generateOpenApiCode", "--stacktrace")
            .forwardOutput()
            .build()
        
        // Verify task succeeded
        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":generateOpenApiCode")?.outcome,
            "generateOpenApiCode task should succeed"
        )
        
        // Verify files were generated
        val generatedDir = File(testProjectDir, "build/generated/openapi")
        assertTrue(generatedDir.exists(), "Generated directory should exist")
        
        val modelFile = File(generatedDir, "com/example/api/models/Pet.kt")
        assertTrue(modelFile.exists(), "Pet model should be generated")
        
        val clientFile = File(generatedDir, "com/example/api/client/PetStoreClient.kt")
        assertTrue(clientFile.exists(), "Client should be generated")
        
        // Verify content
        val modelContent = modelFile.readText()
        assertTrue(modelContent.contains("data class Pet"), "Pet should be a data class")
        assertTrue(modelContent.contains("val id: Long"), "Pet should have id property")
        assertTrue(modelContent.contains("val name: String"), "Pet should have name property")
        
        val clientContent = clientFile.readText()
        assertTrue(clientContent.contains("class PetStoreClient"), "Should generate PetStoreClient class")
        assertTrue(clientContent.contains("suspend fun listPets"), "Should generate listPets method")
    }
    
    @Test
    fun `should handle validation errors gracefully`() {
        // Create build file with invalid spec
        val buildFile = File(testProjectDir, "build.gradle.kts")
        buildFile.writeText("""
            plugins {
                id("org.jetbrains.kotlin.jvm") version "2.1.21"
                id("com.codingfeline.openapi")
            }
            
            repositories {
                mavenLocal()
                mavenCentral()
            }
            
            openApiKotlin {
                inputSpec = file("invalid.json")
                outputDir = layout.buildDirectory.file("generated/openapi").get().asFile
                packageName = "com.example.api"
            }
        """.trimIndent())
        
        // Create settings file
        val settingsFile = File(testProjectDir, "settings.gradle.kts")
        settingsFile.writeText("""
            rootProject.name = "test-project"
            
            pluginManagement {
                repositories {
                    mavenLocal()
                    gradlePluginPortal()
                }
                
                resolutionStrategy {
                    eachPlugin {
                        if (requested.id.id == "com.codingfeline.openapi") {
                            useModule("com.codingfeline.openapikotlin:openapikotlin-gradle-plugin:1.0.0-SNAPSHOT")
                        }
                    }
                }
            }
        """.trimIndent())
        
        // Create invalid OpenAPI spec
        val specFile = File(testProjectDir, "invalid.json")
        specFile.writeText("""
            {
              "not": "a valid openapi spec"
            }
        """.trimIndent())
        
        // Run the task and expect failure
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("generateOpenApiCode", "--stacktrace")
            .forwardOutput()
            .buildAndFail()
        
        // Verify task failed
        assertEquals(
            TaskOutcome.FAILED,
            result.task(":generateOpenApiCode")?.outcome,
            "Task should fail with invalid spec"
        )
    }
}