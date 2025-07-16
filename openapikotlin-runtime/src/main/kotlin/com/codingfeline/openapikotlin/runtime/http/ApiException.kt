package com.codingfeline.openapikotlin.runtime.http

import io.ktor.client.plugins.*
import io.ktor.http.*

/**
 * Exception thrown when API requests fail
 */
class ApiException(
    val statusCode: HttpStatusCode,
    override val message: String,
    val body: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * Returns true if the error is a client error (4xx status code)
     */
    fun isClientError(): Boolean = statusCode.value in 400..499
    
    /**
     * Returns true if the error is a server error (5xx status code)
     */
    fun isServerError(): Boolean = statusCode.value in 500..599
    
    companion object {
        /**
         * Creates an ApiException from a ResponseException
         */
        fun from(exception: ResponseException): ApiException {
            return ApiException(
                statusCode = exception.response.status,
                message = exception.response.status.description,
                body = exception.response.toString()
            )
        }
        
        /**
         * Creates an ApiException from any exception
         */
        fun from(exception: Exception): ApiException {
            return when (exception) {
                is ResponseException -> from(exception)
                is ApiException -> exception
                else -> ApiException(
                    statusCode = HttpStatusCode.InternalServerError,
                    message = exception.message ?: "Unknown error",
                    body = null,
                    cause = exception
                )
            }
        }
    }
}