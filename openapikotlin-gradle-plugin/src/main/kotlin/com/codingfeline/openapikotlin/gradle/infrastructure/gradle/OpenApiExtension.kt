package com.codingfeline.openapikotlin.gradle.infrastructure.gradle

import com.codingfeline.openapikotlin.gradle.ClientConfig
import com.codingfeline.openapikotlin.gradle.ModelsConfig
import com.codingfeline.openapikotlin.gradle.ValidationConfig
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import java.io.File
import javax.inject.Inject

/**
 * Extension for configuring the OpenAPI Kotlin plugin
 */
abstract class OpenApiExtension @Inject constructor(project: Project) {
    
    /**
     * The OpenAPI specification file to use as input
     */
    abstract val inputSpec: RegularFileProperty
    
    /**
     * The output directory for generated code
     */
    abstract val outputDir: Property<File>
    
    /**
     * The base package name for generated code
     */
    abstract val packageName: Property<String>
    
    /**
     * Configuration for model generation
     */
    val models: Property<ModelsConfig> = project.objects.property(ModelsConfig::class.java).apply {
        convention(ModelsConfig())
    }
    
    /**
     * Configuration for client generation
     */
    val client: Property<ClientConfig> = project.objects.property(ClientConfig::class.java).apply {
        convention(ClientConfig())
    }
    
    /**
     * Configuration for validation
     */
    val validation: Property<ValidationConfig> = project.objects.property(ValidationConfig::class.java).apply {
        convention(ValidationConfig())
    }
    
    init {
        // Set default values
        outputDir.convention(project.layout.buildDirectory.file("generated/openapi").map { it.asFile })
        packageName.convention("com.example.api")
    }
    
    /**
     * Configure models generation
     */
    fun models(action: ModelsConfig.() -> Unit) {
        models.set(models.get().apply(action))
    }
    
    /**
     * Configure client generation
     */
    fun client(action: ClientConfig.() -> Unit) {
        client.set(client.get().apply(action))
    }
    
    /**
     * Configure validation
     */
    fun validation(action: ValidationConfig.() -> Unit) {
        validation.set(validation.get().apply(action))
    }
}