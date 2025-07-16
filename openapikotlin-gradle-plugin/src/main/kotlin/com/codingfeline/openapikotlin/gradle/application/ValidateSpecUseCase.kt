package com.codingfeline.openapikotlin.gradle.application

import com.codingfeline.openapikotlin.gradle.domain.service.ValidationResult
import com.codingfeline.openapikotlin.gradle.domain.service.ValidationService
import java.io.File

/**
 * Use case for validating OpenAPI specifications
 */
class ValidateSpecUseCase(
    private val parser: OpenApiParser,
    private val validationService: ValidationService
) {
    
    /**
     * Validates an OpenAPI specification file
     */
    fun execute(specFile: File): ValidationResult {
        // 1. Parse the specification
        val spec = parser.parse(specFile)
        
        // 2. Validate the specification
        val result = validationService.validateSpec(spec)
        
        // 3. Validate individual schemas
        val schemaResults = spec.getAllSchemas().map { (name, schema) ->
            validationService.validateSchema(name, schema)
        }
        
        // 4. Combine results
        val allErrors = result.errors + schemaResults.flatMap { it.errors }
        val allWarnings = result.warnings + schemaResults.flatMap { it.warnings }
        
        return ValidationResult(
            isValid = result.isValid && schemaResults.all { it.isValid },
            errors = allErrors,
            warnings = allWarnings
        )
    }
    
    /**
     * Formats validation results for display
     */
    fun formatResults(result: ValidationResult): String {
        val output = StringBuilder()
        
        if (result.isSuccess()) {
            output.appendLine("✓ Specification is valid")
        } else {
            output.appendLine("✗ Specification validation failed")
        }
        
        if (result.errors.isNotEmpty()) {
            output.appendLine("\nErrors:")
            result.errors.forEach { error ->
                output.appendLine("  - ${error.path}: ${error.message}")
            }
        }
        
        if (result.warnings.isNotEmpty()) {
            output.appendLine("\nWarnings:")
            result.warnings.forEach { warning ->
                output.appendLine("  - ${warning.path}: ${warning.message}")
            }
        }
        
        return output.toString()
    }
}