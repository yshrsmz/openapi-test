package com.codingfeline.openapikotlin.gradle.infrastructure.generator

import com.codingfeline.openapikotlin.gradle.infrastructure.parser.SwaggerParserAdapter
import com.codingfeline.openapikotlin.gradle.ClientConfig
import com.codingfeline.openapikotlin.gradle.ModelsConfig
import com.codingfeline.openapikotlin.gradle.domain.service.GeneratedFile
import io.swagger.v3.parser.OpenAPIV3Parser
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SchemaCompositionTest {
    
    @TempDir
    lateinit var tempDir: File
    
    private val parser = SwaggerParserAdapter()
    private val typeMapper = KotlinPoetTypeMapper("com.test")
    private val modelsConfig = ModelsConfig(
        generateDataAnnotations = true,
        generateDefaultValues = true,
        useKotlinxDatetime = true
    )
    private val clientConfig = ClientConfig(
        clientClassName = "TestApiClient",
        generateErrorHandling = true,
        generateAuthHelpers = true
    )
    
    private fun GeneratedFile.writeToString(): String {
        return this.content
    }
    
    private fun createTempSpecFile(content: String): File {
        val file = File(tempDir, "spec.yaml")
        file.writeText(content)
        return file
    }
    
    @Test
    fun `test allOf schema composition with simple properties`() {
        val spec = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                BaseModel:
                  type: object
                  properties:
                    id:
                      type: string
                    createdAt:
                      type: string
                      format: date-time
                ExtendedModel:
                  allOf:
                    - ${'$'}ref: '#/components/schemas/BaseModel'
                    - type: object
                      properties:
                        name:
                          type: string
                        description:
                          type: string
        """.trimIndent()
        
        val openAPI = OpenAPIV3Parser().readContents(spec).openAPI
        val domainSpec = parser.parse(createTempSpecFile(spec))
        val generator = KotlinPoetModelGenerator(typeMapper, modelsConfig)
        val schemas = domainSpec.components?.schemas ?: emptyMap()
        val models = generator.generateModels(schemas, "com.test.models")
        
        // Debug: print all generated models
        println("Generated ${models.size} models:")
        models.forEach { model ->
            println("Model path: ${model.relativePath}")
            println("First 200 chars: ${model.content.take(200)}")
            println("---")
        }
        
        // Should generate both BaseModel and ExtendedModel
        assertEquals(2, models.size, "Should generate 2 models")
        
        val extendedModel = models.find { it.writeToString().contains("class ExtendedModel") }
        assertNotNull(extendedModel, "Should find ExtendedModel")
        
        val content = extendedModel.writeToString()
        
        // Debug: print the generated content
        println("Generated ExtendedModel content:")
        println(content)
        
        // Should have all properties from base and extended
        assertTrue(content.contains("public val id: String"), "Should contain id property")
        assertTrue(content.contains("public val createdAt: Instant"), "Should contain createdAt property")
        assertTrue(content.contains("public val name: String"), "Should contain name property") 
        assertTrue(content.contains("public val description: String"), "Should contain description property")
    }
    
    @Test
    fun `test allOf with multiple schema references`() {
        val spec = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Timestamped:
                  type: object
                  properties:
                    createdAt:
                      type: string
                      format: date-time
                    updatedAt:
                      type: string
                      format: date-time
                Identifiable:
                  type: object
                  properties:
                    id:
                      type: string
                    uuid:
                      type: string
                      format: uuid
                Resource:
                  allOf:
                    - ${'$'}ref: '#/components/schemas/Timestamped'
                    - ${'$'}ref: '#/components/schemas/Identifiable'
                    - type: object
                      properties:
                        name:
                          type: string
                        status:
                          type: string
                          enum: [active, inactive]
        """.trimIndent()
        
        val openAPI = OpenAPIV3Parser().readContents(spec).openAPI
        val domainSpec = parser.parse(createTempSpecFile(spec))
        val generator = KotlinPoetModelGenerator(typeMapper, modelsConfig)
        val schemas = domainSpec.components?.schemas ?: emptyMap()
        val models = generator.generateModels(schemas, "com.test.models")
        
        // Debug: print all generated models
        println("Multiple references test - Generated ${models.size} models:")
        models.forEach { model ->
            println("Model path: ${model.relativePath}")
            if (model.relativePath.contains("Resource")) {
                println("Resource content:")
                println(model.content)
            }
        }
        
        val resource = models.find { it.writeToString().contains("class Resource") }
        assertNotNull(resource, "Should find Resource model")
        
        val content = resource.writeToString()
        
        // Should have all properties from all schemas
        assertTrue(content.contains("public val createdAt: Instant"), "Should contain createdAt")
        assertTrue(content.contains("public val updatedAt: Instant"), "Should contain updatedAt")
        assertTrue(content.contains("public val id: String"), "Should contain id")
        assertTrue(content.contains("public val uuid: String"), "Should contain uuid")
        assertTrue(content.contains("public val name: String"), "Should contain name")
        assertTrue(content.contains("public val status: ResourceStatus"), "Should contain status")
    }
    
    @Test
    fun `test oneOf schema composition generates sealed interface`() {
        val spec = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Pet:
                  oneOf:
                    - ${'$'}ref: '#/components/schemas/Cat'
                    - ${'$'}ref: '#/components/schemas/Dog'
                  discriminator:
                    propertyName: petType
                Cat:
                  type: object
                  required: [petType, meow]
                  properties:
                    petType:
                      type: string
                    meow:
                      type: string
                Dog:
                  type: object
                  required: [petType, bark]
                  properties:
                    petType:
                      type: string
                    bark:
                      type: string
        """.trimIndent()
        
        val openAPI = OpenAPIV3Parser().readContents(spec).openAPI
        val domainSpec = parser.parse(createTempSpecFile(spec))
        val generator = KotlinPoetModelGenerator(typeMapper, modelsConfig)
        val schemas = domainSpec.components?.schemas ?: emptyMap()
        val models = generator.generateModels(schemas, "com.test.models")
        
        // Should generate Pet as sealed interface
        val pet = models.find { it.writeToString().contains("sealed interface Pet") }
        assertNotNull(pet)
        assertTrue(pet.writeToString().contains("public sealed interface Pet"))
        
        // Cat and Dog should implement Pet
        val cat = models.find { it.writeToString().contains("class Cat") }
        assertNotNull(cat)
        assertTrue(cat.writeToString().contains("public data class Cat") && cat.writeToString().contains(": Pet"))
        
        val dog = models.find { it.writeToString().contains("class Dog") }
        assertNotNull(dog)
        assertTrue(dog.writeToString().contains("public data class Dog") && dog.writeToString().contains(": Pet"))
    }
    
    @Test
    fun `test anyOf schema composition generates wrapper class`() {
        val spec = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                ValidationError:
                  anyOf:
                    - type: string
                    - type: array
                      items:
                        type: string
                    - type: object
                      properties:
                        code:
                          type: string
                        message:
                          type: string
        """.trimIndent()
        
        val openAPI = OpenAPIV3Parser().readContents(spec).openAPI
        val domainSpec = parser.parse(createTempSpecFile(spec))
        val generator = KotlinPoetModelGenerator(typeMapper, modelsConfig)
        val schemas = domainSpec.components?.schemas ?: emptyMap()
        val models = generator.generateModels(schemas, "com.test.models")
        
        val validationError = models.find { it.writeToString().contains("class ValidationError") }
        assertNotNull(validationError)
        
        val content = validationError.writeToString()
        
        // Should generate a class that can hold any of the types
        assertTrue(content.contains("public data class ValidationError"))
        // Should have custom serializer to handle the different types
        assertTrue(content.contains("@Serializable"))
    }
    
    @Test
    fun `test nested allOf composition`() {
        val spec = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                Base:
                  type: object
                  properties:
                    id:
                      type: string
                Middle:
                  allOf:
                    - ${'$'}ref: '#/components/schemas/Base'
                    - type: object
                      properties:
                        name:
                          type: string
                Final:
                  allOf:
                    - ${'$'}ref: '#/components/schemas/Middle'
                    - type: object
                      properties:
                        description:
                          type: string
        """.trimIndent()
        
        val openAPI = OpenAPIV3Parser().readContents(spec).openAPI
        val domainSpec = parser.parse(createTempSpecFile(spec))
        val generator = KotlinPoetModelGenerator(typeMapper, modelsConfig)
        val schemas = domainSpec.components?.schemas ?: emptyMap()
        val models = generator.generateModels(schemas, "com.test.models")
        
        // Debug: print all generated models
        println("Nested allOf test - Generated ${models.size} models:")
        models.forEach { model ->
            println("Model: ${model.relativePath}")
            if (model.relativePath.contains("Final")) {
                println("Final model content:")
                println(model.content)
            }
        }
        
        val finalModel = models.find { it.writeToString().contains("class Final") }
        assertNotNull(finalModel, "Should find Final model")
        
        val content = finalModel.writeToString()
        
        // Should have all properties from all levels
        assertTrue(content.contains("public val id: String"), "Should contain id from Base")
        assertTrue(content.contains("public val name: String"), "Should contain name from Middle")
        assertTrue(content.contains("public val description: String"), "Should contain description from Final")
    }
    
    @Test
    fun `test allOf with required properties`() {
        val spec = """
            openapi: 3.0.0
            info:
              title: Test API
              version: 1.0.0
            components:
              schemas:
                BaseRequired:
                  type: object
                  required: [id]
                  properties:
                    id:
                      type: string
                    optional:
                      type: string
                ExtendedRequired:
                  allOf:
                    - ${'$'}ref: '#/components/schemas/BaseRequired'
                    - type: object
                      required: [name]
                      properties:
                        name:
                          type: string
                        anotherOptional:
                          type: string
        """.trimIndent()
        
        val openAPI = OpenAPIV3Parser().readContents(spec).openAPI
        val domainSpec = parser.parse(createTempSpecFile(spec))
        val generator = KotlinPoetModelGenerator(typeMapper, modelsConfig)
        val schemas = domainSpec.components?.schemas ?: emptyMap()
        val models = generator.generateModels(schemas, "com.test.models")
        
        val extended = models.find { it.writeToString().contains("class ExtendedRequired") }
        assertNotNull(extended)
        
        val content = extended.writeToString()
        
        // Required properties should not be nullable
        assertTrue(content.contains("public val id: String,"))
        assertTrue(content.contains("public val name: String,"))
        
        // Optional properties should be nullable with default
        assertTrue(content.contains("public val optional: String? = null"))
        assertTrue(content.contains("public val anotherOptional: String? = null"))
    }
}