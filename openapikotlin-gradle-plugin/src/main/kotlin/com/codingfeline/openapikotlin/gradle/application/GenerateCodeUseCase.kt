package com.codingfeline.openapikotlin.gradle.application

import com.codingfeline.openapikotlin.gradle.ValidationConfig
import com.codingfeline.openapikotlin.gradle.domain.model.OpenApiSpec
import com.codingfeline.openapikotlin.gradle.domain.service.CodeGenerationService
import com.codingfeline.openapikotlin.gradle.domain.service.GeneratedFile
import com.codingfeline.openapikotlin.gradle.domain.service.ValidationService
import com.codingfeline.openapikotlin.gradle.domain.value.PackageName
import java.io.File

/**
 * Use case for generating code from OpenAPI specification
 */
class GenerateCodeUseCase(
    private val parser: OpenApiParser,
    private val modelGenerator: CodeGenerationService,
    private val clientGenerator: CodeGenerationService,
    private val fileWriter: FileWriter,
    private val validationService: ValidationService? = null
) {
    
    /**
     * Executes the code generation process
     */
    fun execute(
        specFile: File,
        outputDirectory: File,
        packageName: String,
        validationConfig: ValidationConfig
    ) {
        // 1. Parse the OpenAPI specification
        val spec = parser.parse(specFile)
        
        // 2. Validate the specification if validation is enabled
        if (validationConfig.validateSpec && validationService != null) {
            val validationResult = validationService.validateSpec(spec)
            
            if (!validationResult.isSuccess()) {
                val errors = validationResult.errors.joinToString("\n") { 
                    "${it.path}: ${it.message}" 
                }
                throw IllegalArgumentException("Specification validation failed:\n$errors")
            }
            
            if (validationResult.hasWarnings() && validationConfig.failOnWarnings) {
                val warnings = validationResult.warnings.joinToString("\n") { 
                    "${it.path}: ${it.message}" 
                }
                throw IllegalArgumentException("Specification has warnings:\n$warnings")
            }
        }
        
        // 3. Create package structure
        val basePackage = PackageName(packageName)
        val modelsPackage = basePackage.append("models")
        val clientPackage = basePackage.append("client")
        
        // 4. Generate model classes
        val schemas = spec.getAllSchemas()
        val modelFiles = modelGenerator.generateModels(schemas, modelsPackage.value)
        
        // 4.5. Generate special types if needed
        val specialTypesGenerator = com.codingfeline.openapikotlin.gradle.infrastructure.generator.SpecialTypesGenerator()
        val specialTypeFiles = specialTypesGenerator.generateModels(spec, modelsPackage.value)
        
        // 5. Generate client code
        val operations = spec.getAllOperationsWithContext()
        val clientFile = clientGenerator.generateClient(spec, operations, clientPackage.value)
        
        // 6. Generate auth helpers if needed
        val authFiles = if (spec.usesOAuth2()) {
            clientGenerator.generateAuthHelpers(spec, clientPackage.value)
        } else {
            emptyList()
        }
        
        // 7. Write all generated files
        val allFiles = modelFiles + specialTypeFiles + clientFile + authFiles
        allFiles.forEach { generatedFile ->
            fileWriter.write(generatedFile, outputDirectory)
        }
        
        // 8. Log summary
        println("Code generation completed:")
        println("  - Generated ${modelFiles.size} model classes")
        println("  - Generated ${specialTypeFiles.size} special type files")
        println("  - Generated 1 client class")
        println("  - Generated ${authFiles.size} auth helper classes")
        println("  - Total files: ${allFiles.size}")
    }
}

/**
 * Interface for parsing OpenAPI specifications
 */
interface OpenApiParser {
    /**
     * Parses an OpenAPI specification file
     */
    fun parse(specFile: File): OpenApiSpec
}

/**
 * Interface for writing generated files
 */
interface FileWriter {
    /**
     * Writes a generated file to the output directory
     */
    fun write(file: GeneratedFile, outputDirectory: File)
}