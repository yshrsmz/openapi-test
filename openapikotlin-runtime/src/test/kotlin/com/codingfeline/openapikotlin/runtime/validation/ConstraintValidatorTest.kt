package com.codingfeline.openapikotlin.runtime.validation

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ConstraintValidatorTest {
    
    private val validator = ConstraintValidator()
    
    @Test
    fun `should validate minimum value constraint`() {
        // Should pass
        validator.validateMinimum("age", 25, minimum = 18)
        validator.validateMinimum("score", 100.5, minimum = 0.0)
        
        // Should fail
        val exception = assertThrows<ValidationException> {
            validator.validateMinimum("age", 15, minimum = 18)
        }
        assertThat(exception.errors[0].field).isEqualTo("age")
        assertThat(exception.errors[0].message).contains("must be at least 18")
    }
    
    @Test
    fun `should validate maximum value constraint`() {
        // Should pass
        validator.validateMaximum("age", 25, maximum = 100)
        validator.validateMaximum("score", 85.5, maximum = 100.0)
        
        // Should fail
        val exception = assertThrows<ValidationException> {
            validator.validateMaximum("score", 105, maximum = 100)
        }
        assertThat(exception.errors[0].field).isEqualTo("score")
        assertThat(exception.errors[0].message).contains("must not exceed 100")
    }
    
    @Test
    fun `should validate string length constraints`() {
        // Should pass
        validator.validateLength("username", "john_doe", minLength = 3, maxLength = 20)
        validator.validateLength("code", "ABC", minLength = 3, maxLength = 3)
        
        // Should fail - too short
        val shortException = assertThrows<ValidationException> {
            validator.validateLength("username", "ab", minLength = 3)
        }
        assertThat(shortException.errors[0].message).contains("at least 3 characters")
        
        // Should fail - too long
        val longException = assertThrows<ValidationException> {
            validator.validateLength("username", "verylongusername12345", maxLength = 20)
        }
        assertThat(longException.errors[0].message).contains("not exceed 20 characters")
    }
    
    @Test
    fun `should validate pattern constraint`() {
        // Should pass
        validator.validatePattern("email", "test@example.com", pattern = "^[^@]+@[^@]+\\.[^@]+$")
        validator.validatePattern("phone", "123-456-7890", pattern = "\\d{3}-\\d{3}-\\d{4}")
        
        // Should fail
        val exception = assertThrows<ValidationException> {
            validator.validatePattern("email", "invalid-email", pattern = "^[^@]+@[^@]+\\.[^@]+$")
        }
        assertThat(exception.errors[0].field).isEqualTo("email")
        assertThat(exception.errors[0].message).contains("must match pattern")
    }
    
    @Test
    fun `should validate required fields`() {
        // Should pass
        validator.validateRequired("name", "John")
        validator.validateRequired("age", 25)
        validator.validateRequired("items", listOf("item1"))
        
        // Should fail - null value
        val nullException = assertThrows<ValidationException> {
            validator.validateRequired("name", null)
        }
        assertThat(nullException.errors[0].message).contains("is required")
        
        // Should fail - empty string
        val emptyException = assertThrows<ValidationException> {
            validator.validateRequired("name", "")
        }
        assertThat(emptyException.errors[0].message).contains("is required")
        
        // Should fail - empty collection
        val emptyListException = assertThrows<ValidationException> {
            validator.validateRequired("items", emptyList<String>())
        }
        assertThat(emptyListException.errors[0].message).contains("is required")
    }
    
    @Test
    fun `should validate multiple constraints`() {
        val errors = mutableListOf<ValidationError>()
        
        // Collect all validation errors
        try {
            validator.validateRequired("name", "")
        } catch (e: ValidationException) {
            errors.addAll(e.errors)
        }
        
        try {
            validator.validateMinimum("age", 15, minimum = 18)
        } catch (e: ValidationException) {
            errors.addAll(e.errors)
        }
        
        try {
            validator.validatePattern("email", "invalid", pattern = "^[^@]+@[^@]+\\.[^@]+$")
        } catch (e: ValidationException) {
            errors.addAll(e.errors)
        }
        
        assertThat(errors).hasSize(3)
        
        // Can throw combined exception
        if (errors.isNotEmpty()) {
            val combinedException = ValidationException(errors)
            assertThat(combinedException.errors).hasSize(3)
            assertThat(combinedException.message).contains("3 errors")
        }
    }
}