package com.codingfeline.openapikotlin.gradle

import com.codingfeline.openapikotlin.gradle.infrastructure.gradle.GenerateTask
import com.codingfeline.openapikotlin.gradle.infrastructure.gradle.OpenApiExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin for generating Kotlin code from OpenAPI specifications
 */
class OpenApiKotlinPlugin : Plugin<Project> {
    
    override fun apply(project: Project) {
        // Create extension
        val extension = project.extensions.create("openApiKotlin", OpenApiExtension::class.java, project)
        
        // Register the code generation task
        project.tasks.register("generateOpenApiCode", GenerateTask::class.java) { task ->
            task.group = "openapi"
            task.description = "Generates Kotlin code from OpenAPI specification"
            
            // Configure task inputs/outputs
            task.inputSpec.set(extension.inputSpec)
            task.outputDir.set(extension.outputDir)
            task.packageName.set(extension.packageName)
            task.modelsConfig.set(extension.models)
            task.clientConfig.set(extension.client)
            task.validationConfig.set(extension.validation)
            
            // Ensure output directory is cleared before generation
            task.doFirst {
                task.outputDir.get().deleteRecursively()
                task.outputDir.get().mkdirs()
            }
        }
        
        // Configure Kotlin source sets to include generated code
        project.afterEvaluate {
            project.extensions.findByType(org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension::class.java)?.let { kotlinExt ->
                kotlinExt.sourceSets.getByName("main").kotlin.srcDir(extension.outputDir.get())
            }
        }
    }
}