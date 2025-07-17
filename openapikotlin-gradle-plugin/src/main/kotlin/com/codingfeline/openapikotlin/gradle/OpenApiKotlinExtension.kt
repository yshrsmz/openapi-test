package com.codingfeline.openapikotlin.gradle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import java.io.File

/**
 * Extension for configuring the OpenAPI Kotlin code generator
 */
abstract class OpenApiKotlinExtension {
    /**
     * OpenAPI specification file (required)
     */
    abstract val inputSpec: RegularFileProperty
    
    /**
     * Output directory for generated code
     */
    abstract val outputDir: Property<File>
    
    /**
     * Base package name for generated code
     */
    abstract val packageName: Property<String>
    
    /**
     * Model generation configuration
     */
    abstract val models: Property<ModelsConfig>
    
    /**
     * Client generation configuration
     */
    abstract val client: Property<ClientConfig>
    
    /**
     * Validation configuration
     */
    abstract val validation: Property<ValidationConfig>
    
    /**
     * Configure model generation
     */
    fun models(action: Action<ModelsConfig>) {
        models.get().apply(action::execute)
    }
    
    /**
     * Configure client generation
     */
    fun client(action: Action<ClientConfig>) {
        client.get().apply(action::execute)
    }
    
    /**
     * Configure validation
     */
    fun validation(action: Action<ValidationConfig>) {
        validation.get().apply(action::execute)
    }
}

/**
 * Model generation configuration
 */
data class ModelsConfig(
    var generateDataAnnotations: Boolean = true,
    var generateDefaultValues: Boolean = true,
    var useKotlinxDatetime: Boolean = true,
    var generateValidation: Boolean = false,
    var useJsonElementForDynamicTypes: Boolean = false,
    var dynamicTypeHandling: DynamicTypeHandling = DynamicTypeHandling.WARN,
    var schemaTypeOverrides: Map<String, String> = emptyMap()
) : java.io.Serializable

/**
 * How to handle dynamic/untyped schemas when useJsonElementForDynamicTypes is false
 */
enum class DynamicTypeHandling {
    /**
     * Generate Any type (will fail at runtime)
     */
    ALLOW,
    
    /**
     * Generate Any type with warning about runtime failure
     */
    WARN,
    
    /**
     * Fail code generation with helpful error message
     */
    FAIL
}

/**
 * Client generation configuration
 */
data class ClientConfig(
    var generateClient: Boolean = true,
    var clientClassName: String = "ApiClient",
    var generateErrorHandling: Boolean = true,
    var generateAuthHelpers: Boolean = true,
    var useCoroutines: Boolean = true,
    var generateSeparateClients: Boolean = false
) : java.io.Serializable

/**
 * Validation configuration
 */
data class ValidationConfig(
    var failOnWarnings: Boolean = false,
    var strict: Boolean = true,
    var validateSpec: Boolean = true
) : java.io.Serializable

/**
 * Creates the OpenAPI extension
 */
internal fun Project.createOpenApiExtension(): OpenApiKotlinExtension {
    return extensions.create("openApi", OpenApiKotlinExtension::class.java).apply {
        outputDir.convention(layout.buildDirectory.dir("generated/openapi").map { it.asFile })
        models.convention(ModelsConfig())
        client.convention(ClientConfig())
        validation.convention(ValidationConfig())
    }
}