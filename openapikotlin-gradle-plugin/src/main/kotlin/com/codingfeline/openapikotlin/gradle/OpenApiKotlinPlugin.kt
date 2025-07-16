package com.codingfeline.openapikotlin.gradle

import com.codingfeline.openapikotlin.gradle.infrastructure.gradle.GenerateTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/**
 * Gradle plugin for generating Kotlin code from OpenAPI specifications
 */
class OpenApiKotlinPlugin : Plugin<Project> {
    
    override fun apply(project: Project) {
        // Create extension
        val extension = project.createOpenApiExtension()
        
        // Register the code generation task
        project.tasks.register<GenerateTask>("generateOpenApiCode") {
            group = "openapi"
            description = "Generates Kotlin code from OpenAPI specification"
            
            // Configure task inputs/outputs
            inputSpec.set(extension.inputSpec)
            outputDir.set(extension.outputDir)
            packageName.set(extension.packageName)
            modelsConfig.set(extension.models)
            clientConfig.set(extension.client)
            validationConfig.set(extension.validation)
            
            // Ensure output directory is cleared before generation
            doFirst {
                outputDir.get().deleteRecursively()
                outputDir.get().mkdirs()
            }
        }
        
        // Configure Kotlin source sets to include generated code
        project.afterEvaluate {
            project.extensions.findByType(org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension::class.java)?.apply {
                sourceSets.getByName("main") {
                    kotlin.srcDir(extension.outputDir)
                }
            }
        }
    }
}