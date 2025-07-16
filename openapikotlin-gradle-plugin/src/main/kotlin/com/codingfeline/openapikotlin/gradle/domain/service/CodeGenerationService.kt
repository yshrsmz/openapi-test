package com.codingfeline.openapikotlin.gradle.domain.service

import com.codingfeline.openapikotlin.gradle.domain.model.OpenApiSpec
import com.codingfeline.openapikotlin.gradle.domain.model.Operation
import com.codingfeline.openapikotlin.gradle.domain.model.Schema
import java.io.File

/**
 * Domain service for generating code from OpenAPI specifications
 */
interface CodeGenerationService {
    
    /**
     * Generates model classes from schemas
     */
    fun generateModels(
        schemas: Map<String, Schema>,
        packageName: String
    ): List<GeneratedFile>
    
    /**
     * Generates client code from operations
     */
    fun generateClient(
        spec: OpenApiSpec,
        operations: List<Operation>,
        packageName: String
    ): GeneratedFile
    
    /**
     * Generates authentication helpers if needed
     */
    fun generateAuthHelpers(
        spec: OpenApiSpec,
        packageName: String
    ): List<GeneratedFile>
}

/**
 * Represents a generated file
 */
data class GeneratedFile(
    val relativePath: String,
    val content: String
) {
    /**
     * Gets the full file path for a given output directory
     */
    fun getFile(outputDirectory: File): File {
        return File(outputDirectory, relativePath)
    }
}