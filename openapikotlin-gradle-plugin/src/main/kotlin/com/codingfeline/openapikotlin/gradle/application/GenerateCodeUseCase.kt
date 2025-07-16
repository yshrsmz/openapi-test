package com.codingfeline.openapikotlin.gradle.application

import com.codingfeline.openapikotlin.gradle.ValidationConfig
import com.codingfeline.openapikotlin.gradle.domain.service.CodeGenerationService
import com.codingfeline.openapikotlin.gradle.domain.service.ValidationService
import java.io.File

/**
 * Use case for generating code from OpenAPI specification
 */
class GenerateCodeUseCase(
    private val parser: OpenApiParser,
    private val modelGenerator: CodeGenerationService,
    private val clientGenerator: CodeGenerationService,
    private val fileWriter: FileWriter
) {
    
    fun execute(
        specFile: File,
        outputDirectory: File,
        packageName: String,
        validationConfig: ValidationConfig
    ) {
        // TODO: Implement code generation logic
        println("Generating code from ${specFile.name} to $outputDirectory")
    }
}

// Temporary interfaces - will be moved to proper locations
interface OpenApiParser
interface FileWriter