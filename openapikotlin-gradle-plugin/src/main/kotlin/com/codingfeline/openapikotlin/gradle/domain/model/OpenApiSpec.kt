package com.codingfeline.openapikotlin.gradle.domain.model

/**
 * Domain model representing an OpenAPI specification
 * This is a simplified model focused on code generation needs
 */
data class OpenApiSpec(
    val openapi: String,
    val info: Info,
    val servers: List<Server>,
    val paths: Map<String, PathItem>,
    val components: Components? = null,
    val security: List<SecurityRequirement>? = null
) {
    /**
     * Gets all schemas defined in components
     */
    fun getAllSchemas(): Map<String, Schema> {
        return components?.schemas ?: emptyMap()
    }
    
    /**
     * Gets all operations from all paths
     */
    fun getAllOperations(): List<Operation> {
        return paths.flatMap { (_, pathItem) ->
            listOfNotNull(
                pathItem.get,
                pathItem.post,
                pathItem.put,
                pathItem.delete,
                pathItem.patch,
                pathItem.head,
                pathItem.options
            )
        }
    }
    
    /**
     * Checks if the spec uses OAuth2 security
     */
    fun usesOAuth2(): Boolean {
        return components?.securitySchemes?.values?.any { 
            it.type == SecuritySchemeType.OAUTH2 
        } ?: false
    }
}

/**
 * API information
 */
data class Info(
    val title: String,
    val version: String,
    val description: String? = null,
    val termsOfService: String? = null,
    val contact: Contact? = null,
    val license: License? = null
)

/**
 * Contact information
 */
data class Contact(
    val name: String? = null,
    val url: String? = null,
    val email: String? = null
)

/**
 * License information
 */
data class License(
    val name: String,
    val url: String? = null
)

/**
 * Server information
 */
data class Server(
    val url: String,
    val description: String? = null,
    val variables: Map<String, ServerVariable>? = null
)

/**
 * Server variable
 */
data class ServerVariable(
    val enum: List<String>? = null,
    val default: String,
    val description: String? = null
)

/**
 * Path item containing operations
 */
data class PathItem(
    val summary: String? = null,
    val description: String? = null,
    val get: Operation? = null,
    val post: Operation? = null,
    val put: Operation? = null,
    val delete: Operation? = null,
    val patch: Operation? = null,
    val head: Operation? = null,
    val options: Operation? = null,
    val trace: Operation? = null,
    val parameters: List<Parameter>? = null
)

/**
 * Reusable components
 */
data class Components(
    val schemas: Map<String, Schema>? = null,
    val responses: Map<String, Response>? = null,
    val parameters: Map<String, Parameter>? = null,
    val requestBodies: Map<String, RequestBody>? = null,
    val headers: Map<String, Header>? = null,
    val securitySchemes: Map<String, SecurityScheme>? = null
)

/**
 * Security requirement
 */
data class SecurityRequirement(
    val requirements: Map<String, List<String>>
)