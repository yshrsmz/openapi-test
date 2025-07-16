package com.codingfeline.openapikotlin.gradle.domain.model

/**
 * Represents an operation with its HTTP context (path and method)
 */
data class OperationContext(
    val operation: Operation,
    val path: String,
    val method: HttpMethod
)

/**
 * HTTP methods supported by OpenAPI
 */
enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS,
    TRACE
}