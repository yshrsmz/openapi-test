package com.codingfeline.openapikotlin.gradle.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class OpenApiSpecTest {
    
    @Test
    fun `should create OpenApiSpec with required fields`() {
        val spec = OpenApiSpec(
            openapi = "3.0.0",
            info = Info(
                title = "Pet Store API",
                version = "1.0.0"
            ),
            servers = emptyList(),
            paths = emptyMap(),
            components = null
        )
        
        assertThat(spec.openapi).isEqualTo("3.0.0")
        assertThat(spec.info.title).isEqualTo("Pet Store API")
        assertThat(spec.info.version).isEqualTo("1.0.0")
        assertThat(spec.paths).isEmpty()
    }
    
    @Test
    fun `should get all schemas from components`() {
        val petSchema = Schema(
            type = SchemaType.OBJECT,
            properties = mapOf(
                "id" to Schema(type = SchemaType.INTEGER, format = "int64"),
                "name" to Schema(type = SchemaType.STRING)
            ),
            required = listOf("name")
        )
        
        val categorySchema = Schema(
            type = SchemaType.OBJECT,
            properties = mapOf(
                "id" to Schema(type = SchemaType.INTEGER, format = "int64"),
                "name" to Schema(type = SchemaType.STRING)
            )
        )
        
        val spec = OpenApiSpec(
            openapi = "3.0.0",
            info = Info("Test API", "1.0.0"),
            servers = emptyList(),
            paths = emptyMap(),
            components = Components(
                schemas = mapOf(
                    "Pet" to petSchema,
                    "Category" to categorySchema
                )
            )
        )
        
        val schemas = spec.getAllSchemas()
        assertThat(schemas).hasSize(2)
        assertThat(schemas).containsKey("Pet")
        assertThat(schemas).containsKey("Category")
    }
    
    @Test
    fun `should get all operations from paths`() {
        val getPetOperation = Operation(
            operationId = "getPet",
            summary = "Get a pet by ID",
            parameters = listOf(
                Parameter(
                    name = "petId",
                    `in` = ParameterLocation.PATH,
                    required = true,
                    schema = Schema(type = SchemaType.INTEGER)
                )
            ),
            responses = mapOf(
                "200" to Response(description = "Successful response")
            )
        )
        
        val listPetsOperation = Operation(
            operationId = "listPets",
            summary = "List all pets",
            responses = mapOf(
                "200" to Response(description = "Successful response")
            )
        )
        
        val spec = OpenApiSpec(
            openapi = "3.0.0",
            info = Info("Test API", "1.0.0"),
            servers = emptyList(),
            paths = mapOf(
                "/pets" to PathItem(
                    get = listPetsOperation
                ),
                "/pets/{petId}" to PathItem(
                    get = getPetOperation
                )
            )
        )
        
        val operations = spec.getAllOperations()
        assertThat(operations).hasSize(2)
        assertThat(operations.map { it.operationId }).containsExactly("getPet", "listPets")
    }
    
    @Test
    fun `should identify if spec uses OAuth2 security`() {
        val specWithOAuth2 = OpenApiSpec(
            openapi = "3.0.0",
            info = Info("Test API", "1.0.0"),
            servers = emptyList(),
            paths = emptyMap(),
            components = Components(
                securitySchemes = mapOf(
                    "oauth2" to SecurityScheme(
                        type = SecuritySchemeType.OAUTH2,
                        flows = OAuthFlows(
                            authorizationCode = OAuthFlow(
                                authorizationUrl = "https://auth.example.com/oauth/authorize",
                                tokenUrl = "https://auth.example.com/oauth/token",
                                scopes = mapOf("read" to "Read access")
                            )
                        )
                    )
                )
            )
        )
        
        assertThat(specWithOAuth2.usesOAuth2()).isTrue()
        
        val specWithoutOAuth2 = OpenApiSpec(
            openapi = "3.0.0",
            info = Info("Test API", "1.0.0"),
            servers = emptyList(),
            paths = emptyMap()
        )
        
        assertThat(specWithoutOAuth2.usesOAuth2()).isFalse()
    }
}