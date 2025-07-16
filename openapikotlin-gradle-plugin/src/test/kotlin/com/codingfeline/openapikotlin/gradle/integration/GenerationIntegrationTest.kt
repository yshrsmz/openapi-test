package com.codingfeline.openapikotlin.gradle.integration

import com.codingfeline.openapikotlin.gradle.OpenApiKotlinPlugin
import com.codingfeline.openapikotlin.gradle.infrastructure.gradle.GenerateTask
import com.codingfeline.openapikotlin.gradle.infrastructure.gradle.OpenApiExtension
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.*

class GenerationIntegrationTest {
    
    @TempDir
    lateinit var tempDir: File
    
    private val testProject by lazy { 
        ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .build()
    }
    
    @BeforeEach
    fun setup() {
        // Apply the plugin
        testProject.pluginManager.apply(OpenApiKotlinPlugin::class.java)
    }
    
    @Test
    fun `generates code from simple OpenAPI spec`() {
        // Given - Create a simple OpenAPI spec
        val specFile = File(tempDir, "simple-api.yaml")
        specFile.writeText("""
            openapi: 3.0.0
            info:
              title: Simple API
              version: 1.0.0
            servers:
              - url: https://api.example.com/v1
            paths:
              /hello:
                get:
                  operationId: sayHello
                  summary: Say hello
                  responses:
                    '200':
                      description: Success
                      content:
                        application/json:
                          schema:
                            ${'$'}ref: '#/components/schemas/HelloResponse'
            components:
              schemas:
                HelloResponse:
                  type: object
                  required:
                    - message
                  properties:
                    message:
                      type: string
                      description: The greeting message
                    timestamp:
                      type: string
                      format: date-time
                      description: When the greeting was generated
        """.trimIndent())
        
        val outputDir = File(tempDir, "generated")
        
        // Configure the extension
        testProject.extensions.configure(OpenApiExtension::class.java) { ext ->
            ext.inputSpec.set(specFile)
            ext.packageName.set("com.example.simple")
            ext.outputDir.set(outputDir)
        }
        
        // When - Execute the task
        val task = testProject.tasks.getByName("generateOpenApiCode") as GenerateTask
        task.generate()
        
        // Then - Verify files were generated
        assertTrue(outputDir.exists(), "Output directory should exist")
        
        // Check model file
        val modelFile = File(outputDir, "com/example/simple/models/HelloResponse.kt")
        assertTrue(modelFile.exists(), "HelloResponse model should be generated")
        
        val modelContent = modelFile.readText()
        assertContains(modelContent, "data class HelloResponse")
        assertContains(modelContent, "val message: String")
        assertContains(modelContent, "val timestamp: Instant? = null")
        assertContains(modelContent, "@Serializable")
        
        // Check client file
        val clientFile = File(outputDir, "com/example/simple/client/ApiClient.kt")
        assertTrue(clientFile.exists(), "ApiClient should be generated")
        
        val clientContent = clientFile.readText()
        assertContains(clientContent, "class ApiClient")
        assertContains(clientContent, "suspend fun sayHello()")
        assertContains(clientContent, "httpClient.request")
        assertContains(clientContent, "/hello")
        assertContains(clientContent, "HttpMethod.Get")
    }
    
    @Test
    fun `generates code with authentication`() {
        // Given - OpenAPI spec with OAuth2
        val specFile = File(tempDir, "auth-api.yaml")
        specFile.writeText("""
            openapi: 3.0.0
            info:
              title: Auth API
              version: 1.0.0
            servers:
              - url: https://api.example.com
            components:
              securitySchemes:
                oauth2:
                  type: oauth2
                  flows:
                    authorizationCode:
                      authorizationUrl: https://auth.example.com/authorize
                      tokenUrl: https://auth.example.com/token
                      scopes:
                        read: Read access
                        write: Write access
            security:
              - oauth2: [read, write]
            paths:
              /user:
                get:
                  operationId: getUser
                  responses:
                    '200':
                      description: Success
        """.trimIndent())
        
        val outputDir = File(tempDir, "generated-auth")
        
        // Configure the extension
        testProject.extensions.configure(OpenApiExtension::class.java) { ext ->
            ext.inputSpec.set(specFile)
            ext.packageName.set("com.example.auth")
            ext.outputDir.set(outputDir)
        }
        
        // When - Execute the task
        val task = testProject.tasks.getByName("generateOpenApiCode") as GenerateTask
        task.generate()
        
        // Then - Verify auth files were generated
        val authConfigFile = File(outputDir, "com/example/auth/client/AuthConfig.kt")
        assertTrue(authConfigFile.exists(), "AuthConfig should be generated")
        
        val authConfigContent = authConfigFile.readText()
        assertContains(authConfigContent, "data class OAuth2Config")
        assertContains(authConfigContent, "val authorizationUrl: String")
        assertContains(authConfigContent, "val tokenUrl: String")
        assertContains(authConfigContent, "val scopes: List<String>")
        
        val authHelperFile = File(outputDir, "com/example/auth/client/AuthHelper.kt")
        assertTrue(authHelperFile.exists(), "AuthHelper should be generated")
    }
}