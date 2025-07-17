package com.codingfeline.openapikotlin.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertTrue

class JsonElementGenerationTest {
    @TempDir
    lateinit var testProjectDir: File
    
    @Test
    fun `test generates Map with JsonElement for additionalProperties true`() {
        // Create test project
        val buildFile = File(testProjectDir, "build.gradle.kts")
        buildFile.writeText("""
            plugins {
                id("org.jetbrains.kotlin.jvm") version "2.1.21"
                id("com.codingfeline.openapi") version "1.0.0-SNAPSHOT"
            }
            
            repositories {
                mavenLocal()
                mavenCentral()
            }
            
            openApiKotlin {
                inputSpec = file("openapi.yaml")
                outputDir = file("${'$'}buildDir/generated")
                packageName = "com.test.api"
            }
        """.trimIndent())
        
        // Create OpenAPI spec with additionalProperties: true
        val specFile = File(testProjectDir, "openapi.yaml")
        specFile.writeText("""
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                DynamicObject:
                  type: object
                  additionalProperties: true
        """.trimIndent())
        
        // Run code generation
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments("generateOpenApiCode", "--stacktrace")
            .build()
        
        // Check generated file
        val generatedFile = File(testProjectDir, "build/generated/com/test/api/models/DynamicObject.kt")
        assertTrue(generatedFile.exists(), "Generated file should exist")
        
        val content = generatedFile.readText()
        assertTrue(content.contains("Map<String, JsonElement>"), 
            "Should generate Map<String, JsonElement> for additionalProperties: true")
        assertTrue(content.contains("import kotlinx.serialization.json.JsonElement"), 
            "Should import JsonElement")
    }
}