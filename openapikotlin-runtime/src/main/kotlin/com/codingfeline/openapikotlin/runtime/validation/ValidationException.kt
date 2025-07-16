package com.codingfeline.openapikotlin.runtime.validation

/**
 * Represents a validation error for a specific field
 */
data class ValidationError(
    val field: String?,
    val message: String
)

/**
 * Exception thrown when validation fails
 */
class ValidationException : Exception {
    val errors: List<ValidationError>
    
    constructor(message: String) : super(message) {
        this.errors = listOf(ValidationError(null, message))
    }
    
    constructor(field: String, message: String) : super("Validation failed: $field: $message") {
        this.errors = listOf(ValidationError(field, message))
    }
    
    constructor(errors: List<ValidationError>) : super(formatMessage(errors)) {
        this.errors = errors
    }
    
    companion object {
        private fun formatMessage(errors: List<ValidationError>): String {
            if (errors.isEmpty()) return "Validation failed"
            if (errors.size == 1) {
                val error = errors[0]
                return if (error.field != null) {
                    "Validation failed: ${error.field}: ${error.message}"
                } else {
                    error.message
                }
            }
            
            val errorDetails = errors.joinToString(", ") { error ->
                if (error.field != null) {
                    "${error.field}: ${error.message}"
                } else {
                    error.message
                }
            }
            return "Validation failed with ${errors.size} errors: $errorDetails"
        }
    }
}