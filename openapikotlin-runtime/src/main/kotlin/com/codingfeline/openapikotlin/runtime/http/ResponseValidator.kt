package com.codingfeline.openapikotlin.runtime.http

import io.ktor.http.*

/**
 * Validates HTTP responses and throws appropriate exceptions
 */
class ResponseValidator {
    
    /**
     * Validates the HTTP status code
     * @throws ApiException if the status code indicates an error
     */
    fun validateStatus(status: HttpStatusCode, responseBody: String? = null) {
        if (!status.isSuccess()) {
            throw ApiException(
                statusCode = status,
                message = status.description,
                body = responseBody
            )
        }
    }
    
    /**
     * Validates the content type
     * @throws ApiException if the content type doesn't match expected type
     */
    fun validateContentType(
        actualType: ContentType,
        expectedType: ContentType = ContentType.Application.Json
    ) {
        val actualBase = "${actualType.contentType}/${actualType.contentSubtype}"
        val expectedBase = "${expectedType.contentType}/${expectedType.contentSubtype}"
        
        if (actualBase != expectedBase) {
            throw ApiException(
                statusCode = HttpStatusCode.UnsupportedMediaType,
                message = "Expected content type $expectedBase but got $actualBase",
                body = null
            )
        }
    }
    
    /**
     * Validates both status and content type
     */
    fun validate(
        status: HttpStatusCode,
        contentType: ContentType? = null,
        responseBody: String? = null,
        expectedContentType: ContentType = ContentType.Application.Json
    ) {
        validateStatus(status, responseBody)
        contentType?.let { validateContentType(it, expectedContentType) }
    }
}