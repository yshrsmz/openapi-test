package com.codingfeline.openapikotlin.gradle.domain.service

import com.codingfeline.openapikotlin.gradle.domain.model.OpenApiSpec
import com.codingfeline.openapikotlin.gradle.domain.model.Schema

/**
 * Domain service for validating OpenAPI specifications
 */
interface ValidationService {
    
    /**
     * Validates an OpenAPI specification
     */
    fun validateSpec(spec: OpenApiSpec): ValidationResult
    
    /**
     * Validates a schema
     */
    fun validateSchema(
        schemaName: String,
        schema: Schema
    ): ValidationResult
    
    /**
     * Checks if a schema is supported for code generation
     */
    fun isSchemaSupported(schema: Schema): Boolean
}

/**
 * Result of validation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<ValidationError> = emptyList(),
    val warnings: List<ValidationWarning> = emptyList()
) {
    /**
     * Checks if validation passed without errors
     */
    fun isSuccess(): Boolean = isValid && errors.isEmpty()
    
    /**
     * Checks if there are any warnings
     */
    fun hasWarnings(): Boolean = warnings.isNotEmpty()
}

/**
 * Validation error
 */
data class ValidationError(
    val path: String,
    val message: String,
    val severity: Severity = Severity.ERROR
)

/**
 * Validation warning
 */
data class ValidationWarning(
    val path: String,
    val message: String,
    val severity: Severity = Severity.WARNING
)

/**
 * Severity levels
 */
enum class Severity {
    ERROR,
    WARNING,
    INFO
}