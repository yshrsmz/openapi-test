package com.codingfeline.openapikotlin.gradle

import com.codingfeline.openapikotlin.gradle.infrastructure.gradle.GenerateTask
import com.codingfeline.openapikotlin.gradle.infrastructure.gradle.OpenApiExtension
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.*

class OpenApiKotlinPluginTest {
    
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
    fun `plugin creates extension`() {
        // Then
        val extension = testProject.extensions.findByType(OpenApiExtension::class.java)
        assertNotNull(extension, "Plugin should create openApiKotlin extension")
    }
    
    @Test
    fun `plugin registers generateOpenApiCode task`() {
        // Then
        val task = testProject.tasks.findByName("generateOpenApiCode")
        assertNotNull(task, "Plugin should register generateOpenApiCode task")
        assertTrue(task is GenerateTask, "Task should be of type GenerateTask")
        assertEquals("openapi", task.group, "Task should be in openapi group")
    }
    
    @Test
    fun `extension has default values`() {
        // Given
        val extension = testProject.extensions.getByType(OpenApiExtension::class.java)
        
        // Then
        assertEquals("com.example.api", extension.packageName.get())
        assertEquals(
            testProject.layout.buildDirectory.file("generated/openapi").get().asFile,
            extension.outputDir.get()
        )
        
        // Check default configs
        val modelsConfig = extension.models.get()
        assertTrue(modelsConfig.generateDataAnnotations)
        assertTrue(modelsConfig.generateDefaultValues)
        assertTrue(modelsConfig.useKotlinxDatetime)
        assertFalse(modelsConfig.generateValidation)
        
        val clientConfig = extension.client.get()
        assertTrue(clientConfig.generateClient)
        assertEquals("ApiClient", clientConfig.clientClassName)
        assertTrue(clientConfig.generateErrorHandling)
        assertTrue(clientConfig.generateAuthHelpers)
        assertTrue(clientConfig.useCoroutines)
        
        val validationConfig = extension.validation.get()
        assertFalse(validationConfig.failOnWarnings)
        assertTrue(validationConfig.strict)
        assertTrue(validationConfig.validateSpec)
    }
    
    @Test
    fun `extension can be configured`() {
        // Given
        val specFile = File(tempDir, "api.yaml")
        specFile.writeText("openapi: 3.0.0")
        
        // When
        testProject.extensions.configure(OpenApiExtension::class.java) { ext ->
            ext.inputSpec.set(specFile)
            ext.packageName.set("com.test.api")
            ext.outputDir.set(File(tempDir, "generated"))
            
            ext.models.get().apply {
                generateDataAnnotations = false
                useKotlinxDatetime = false
            }
            
            ext.client.get().apply {
                clientClassName = "TestClient"
                useCoroutines = false
            }
            
            ext.validation.get().apply {
                failOnWarnings = true
                strict = false
            }
        }
        
        // Then
        val extension = testProject.extensions.getByType(OpenApiExtension::class.java)
        assertEquals(specFile, extension.inputSpec.get().asFile)
        assertEquals("com.test.api", extension.packageName.get())
        assertEquals(File(tempDir, "generated"), extension.outputDir.get())
        
        val modelsConfig = extension.models.get()
        assertFalse(modelsConfig.generateDataAnnotations)
        assertFalse(modelsConfig.useKotlinxDatetime)
        
        val clientConfig = extension.client.get()
        assertEquals("TestClient", clientConfig.clientClassName)
        assertFalse(clientConfig.useCoroutines)
        
        val validationConfig = extension.validation.get()
        assertTrue(validationConfig.failOnWarnings)
        assertFalse(validationConfig.strict)
    }
    
    @Test
    fun `task is configured from extension`() {
        // Given
        val specFile = File(tempDir, "petstore.yaml")
        specFile.writeText("""
            openapi: 3.0.0
            info:
              title: Petstore
              version: 1.0.0
            paths: {}
        """.trimIndent())
        
        testProject.extensions.configure(OpenApiExtension::class.java) { ext ->
            ext.inputSpec.set(specFile)
            ext.packageName.set("com.petstore.api")
        }
        
        // When
        val task = testProject.tasks.getByName("generateOpenApiCode") as GenerateTask
        
        // Then
        assertEquals(specFile, task.inputSpec.get().asFile)
        assertEquals("com.petstore.api", task.packageName.get())
        assertEquals(
            testProject.layout.buildDirectory.file("generated/openapi").get().asFile,
            task.outputDir.get()
        )
    }
}