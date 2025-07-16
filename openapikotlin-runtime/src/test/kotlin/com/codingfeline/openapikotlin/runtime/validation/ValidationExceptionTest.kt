package com.codingfeline.openapikotlin.runtime.validation

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ValidationExceptionTest {
    
    @Test
    fun `should create ValidationException with single error`() {
        val exception = ValidationException("Invalid field value")
        
        assertThat(exception.message).isEqualTo("Invalid field value")
        assertThat(exception.errors).hasSize(1)
        assertThat(exception.errors[0].field).isNull()
        assertThat(exception.errors[0].message).isEqualTo("Invalid field value")
    }
    
    @Test
    fun `should create ValidationException with field error`() {
        val exception = ValidationException(
            field = "email",
            message = "Invalid email format"
        )
        
        assertThat(exception.message).isEqualTo("Validation failed: email: Invalid email format")
        assertThat(exception.errors).hasSize(1)
        assertThat(exception.errors[0].field).isEqualTo("email")
        assertThat(exception.errors[0].message).isEqualTo("Invalid email format")
    }
    
    @Test
    fun `should create ValidationException with multiple errors`() {
        val errors = listOf(
            ValidationError("name", "Name is required"),
            ValidationError("age", "Age must be positive"),
            ValidationError("email", "Invalid email format")
        )
        val exception = ValidationException(errors)
        
        assertThat(exception.message).contains("3 errors")
        assertThat(exception.errors).hasSize(3)
        assertThat(exception.errors[0].field).isEqualTo("name")
        assertThat(exception.errors[1].field).isEqualTo("age")
        assertThat(exception.errors[2].field).isEqualTo("email")
    }
    
    @Test
    fun `should format error message properly`() {
        val errors = listOf(
            ValidationError("name", "Name is required"),
            ValidationError("age", "Age must be positive")
        )
        val exception = ValidationException(errors)
        
        assertThat(exception.message).isEqualTo(
            "Validation failed with 2 errors: name: Name is required, age: Age must be positive"
        )
    }
}