package com.codingfeline.openapikotlin.gradle.infrastructure.generator

import com.codingfeline.openapikotlin.gradle.ClientConfig
import com.codingfeline.openapikotlin.gradle.domain.service.CodeGenerationService
import com.codingfeline.openapikotlin.gradle.domain.service.GeneratedFile

/**
 * Client generator implementation using KotlinPoet
 */
class KotlinPoetClientGenerator(
    private val typeMapper: KotlinPoetTypeMapper,
    private val config: ClientConfig
) : CodeGenerationService {
    // TODO: Implement client generation
    override fun generateModels(schemas: Map<String, com.codingfeline.openapikotlin.gradle.domain.model.Schema>, packageName: String): List<GeneratedFile> {
        return emptyList()
    }
    
    override fun generateClient(spec: com.codingfeline.openapikotlin.gradle.domain.model.OpenApiSpec, operations: List<com.codingfeline.openapikotlin.gradle.domain.model.Operation>, packageName: String): GeneratedFile {
        return GeneratedFile("Client.kt", "// TODO")
    }
    
    override fun generateAuthHelpers(spec: com.codingfeline.openapikotlin.gradle.domain.model.OpenApiSpec, packageName: String): List<GeneratedFile> {
        return emptyList()
    }
}