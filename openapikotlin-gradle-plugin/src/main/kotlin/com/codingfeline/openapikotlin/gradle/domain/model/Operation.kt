package com.codingfeline.openapikotlin.gradle.domain.model

/**
 * Domain model for OpenAPI operation
 */
data class Operation(
    val operationId: String? = null,
    val summary: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val externalDocs: ExternalDocumentation? = null,
    val parameters: List<Parameter>? = null,
    val requestBody: RequestBody? = null,
    val responses: Map<String, Response>,
    val callbacks: Map<String, Callback>? = null,
    val deprecated: Boolean = false,
    val security: List<SecurityRequirement>? = null,
    val servers: List<Server>? = null
) {
    /**
     * Gets all parameters of a specific type
     */
    fun getParametersByLocation(location: ParameterLocation): List<Parameter> {
        return parameters?.filter { it.`in` == location } ?: emptyList()
    }
    
    /**
     * Checks if the operation has a request body
     */
    fun hasRequestBody(): Boolean = requestBody != null
    
    /**
     * Gets the successful response (2xx status codes)
     */
    fun getSuccessResponse(): Response? {
        return responses.entries
            .firstOrNull { it.key.startsWith("2") }
            ?.value
    }
    
    /**
     * Generates a default operation ID if not provided
     */
    fun getEffectiveOperationId(method: String, path: String): String {
        return operationId ?: generateOperationId(method, path)
    }
    
    private fun generateOperationId(method: String, path: String): String {
        val pathParts = path.split("/")
            .filter { it.isNotBlank() && !it.startsWith("{") }
            .joinToString("") { it.capitalize() }
        
        val paramParts = path.split("/")
            .filter { it.startsWith("{") && it.endsWith("}") }
            .map { it.removeSurrounding("{", "}").capitalize() }
            .joinToString("")
        
        return method.lowercase() + pathParts + 
               (if (paramParts.isNotEmpty()) "By$paramParts" else "")
    }
}

/**
 * Parameter for operations
 */
data class Parameter(
    val name: String,
    val `in`: ParameterLocation,
    val description: String? = null,
    val required: Boolean = false,
    val deprecated: Boolean = false,
    val allowEmptyValue: Boolean = false,
    val style: ParameterStyle? = null,
    val explode: Boolean? = null,
    val allowReserved: Boolean = false,
    val schema: Schema? = null,
    val example: Any? = null,
    val examples: Map<String, Example>? = null
)

/**
 * Parameter locations
 */
enum class ParameterLocation {
    QUERY,
    HEADER,
    PATH,
    COOKIE
}

/**
 * Parameter styles
 */
enum class ParameterStyle {
    MATRIX,
    LABEL,
    FORM,
    SIMPLE,
    SPACE_DELIMITED,
    PIPE_DELIMITED,
    DEEP_OBJECT
}

/**
 * Request body
 */
data class RequestBody(
    val description: String? = null,
    val content: Map<String, MediaType>,
    val required: Boolean = false
)

/**
 * Response
 */
data class Response(
    val description: String,
    val headers: Map<String, Header>? = null,
    val content: Map<String, MediaType>? = null,
    val links: Map<String, Link>? = null
)

/**
 * Media type
 */
data class MediaType(
    val schema: Schema? = null,
    val example: Any? = null,
    val examples: Map<String, Example>? = null,
    val encoding: Map<String, Encoding>? = null
)

/**
 * Header
 */
data class Header(
    val description: String? = null,
    val required: Boolean = false,
    val deprecated: Boolean = false,
    val allowEmptyValue: Boolean = false,
    val style: HeaderStyle = HeaderStyle.SIMPLE,
    val explode: Boolean = false,
    val allowReserved: Boolean = false,
    val schema: Schema? = null,
    val example: Any? = null,
    val examples: Map<String, Example>? = null
)

/**
 * Header styles
 */
enum class HeaderStyle {
    SIMPLE
}

/**
 * Example
 */
data class Example(
    val summary: String? = null,
    val description: String? = null,
    val value: Any? = null,
    val externalValue: String? = null
)

/**
 * Encoding
 */
data class Encoding(
    val contentType: String? = null,
    val headers: Map<String, Header>? = null,
    val style: ParameterStyle? = null,
    val explode: Boolean = false,
    val allowReserved: Boolean = false
)

/**
 * Link
 */
data class Link(
    val operationRef: String? = null,
    val operationId: String? = null,
    val parameters: Map<String, Any>? = null,
    val requestBody: Any? = null,
    val description: String? = null,
    val server: Server? = null
)

/**
 * Callback
 */
data class Callback(
    val expressions: Map<String, PathItem>
)

/**
 * External documentation
 */
data class ExternalDocumentation(
    val description: String? = null,
    val url: String
)