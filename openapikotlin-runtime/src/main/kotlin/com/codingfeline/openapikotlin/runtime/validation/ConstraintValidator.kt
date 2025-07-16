package com.codingfeline.openapikotlin.runtime.validation

/**
 * Runtime validation for OpenAPI constraints
 */
class ConstraintValidator {
    
    /**
     * Validates that a value is not null or empty
     */
    fun validateRequired(field: String, value: Any?) {
        when {
            value == null -> throw ValidationException(field, "$field is required")
            value is String && value.isEmpty() -> throw ValidationException(field, "$field is required")
            value is Collection<*> && value.isEmpty() -> throw ValidationException(field, "$field is required")
        }
    }
    
    /**
     * Validates minimum value constraint
     */
    fun validateMinimum(field: String, value: Number, minimum: Number) {
        if (value.toDouble() < minimum.toDouble()) {
            throw ValidationException(field, "$field must be at least $minimum")
        }
    }
    
    /**
     * Validates maximum value constraint
     */
    fun validateMaximum(field: String, value: Number, maximum: Number) {
        if (value.toDouble() > maximum.toDouble()) {
            throw ValidationException(field, "$field must not exceed $maximum")
        }
    }
    
    /**
     * Validates string length constraints
     */
    fun validateLength(field: String, value: String, minLength: Int? = null, maxLength: Int? = null) {
        minLength?.let {
            if (value.length < it) {
                throw ValidationException(field, "$field must be at least $it characters long")
            }
        }
        
        maxLength?.let {
            if (value.length > it) {
                throw ValidationException(field, "$field must not exceed $it characters")
            }
        }
    }
    
    /**
     * Validates string pattern constraint
     */
    fun validatePattern(field: String, value: String, pattern: String) {
        val regex = Regex(pattern)
        if (!regex.matches(value)) {
            throw ValidationException(field, "$field must match pattern: $pattern")
        }
    }
    
    /**
     * Validates array size constraints
     */
    fun validateArraySize(field: String, value: Collection<*>, minItems: Int? = null, maxItems: Int? = null) {
        minItems?.let {
            if (value.size < it) {
                throw ValidationException(field, "$field must contain at least $it items")
            }
        }
        
        maxItems?.let {
            if (value.size > it) {
                throw ValidationException(field, "$field must not contain more than $it items")
            }
        }
    }
    
    /**
     * Validates all constraints and returns all errors
     */
    fun validateAll(validations: List<() -> Unit>): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        for (validation in validations) {
            try {
                validation()
            } catch (e: ValidationException) {
                errors.addAll(e.errors)
            }
        }
        
        return errors
    }
}