package com.codingfeline.openapikotlin.gradle.infrastructure.gradle

import com.codingfeline.openapikotlin.gradle.ClientConfig
import com.codingfeline.openapikotlin.gradle.ModelsConfig
import com.codingfeline.openapikotlin.gradle.ValidationConfig
import com.codingfeline.openapikotlin.gradle.application.GenerateCodeUseCase
import com.codingfeline.openapikotlin.gradle.infrastructure.generator.KotlinPoetClientGenerator
import com.codingfeline.openapikotlin.gradle.infrastructure.generator.KotlinPoetModelGenerator
import com.codingfeline.openapikotlin.gradle.infrastructure.generator.KotlinPoetTypeMapper
import com.codingfeline.openapikotlin.gradle.infrastructure.io.FileWriter
import com.codingfeline.openapikotlin.gradle.infrastructure.parser.SwaggerParserAdapter
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Gradle task for generating Kotlin code from OpenAPI specification
 */
abstract class GenerateTask : DefaultTask() {
    
    @get:InputFile
    abstract val inputSpec: RegularFileProperty
    
    @get:OutputDirectory
    abstract val outputDir: Property<File>
    
    @get:Input
    abstract val packageName: Property<String>
    
    @get:Input
    abstract val modelsConfig: Property<ModelsConfig>
    
    @get:Input
    abstract val clientConfig: Property<ClientConfig>
    
    @get:Input
    abstract val validationConfig: Property<ValidationConfig>
    
    @TaskAction
    fun generate() {
        val specFile = inputSpec.get().asFile
        val outputDirectory = outputDir.get()
        val basePackage = packageName.get()
        
        logger.lifecycle("Generating Kotlin code from OpenAPI spec: ${specFile.name}")
        
        // Create infrastructure implementations
        val parser = SwaggerParserAdapter()
        val typeMapper = KotlinPoetTypeMapper(basePackage)
        val modelGenerator = KotlinPoetModelGenerator(typeMapper, modelsConfig.get())
        val clientGenerator = KotlinPoetClientGenerator(typeMapper, clientConfig.get())
        val fileWriter = FileWriter()
        
        // Create use case
        val useCase = GenerateCodeUseCase(
            parser = parser,
            modelGenerator = modelGenerator,
            clientGenerator = clientGenerator,
            fileWriter = fileWriter
        )
        
        // Execute generation
        try {
            useCase.execute(
                specFile = specFile,
                outputDirectory = outputDirectory,
                packageName = basePackage,
                validationConfig = validationConfig.get()
            )
            
            logger.lifecycle("Code generation completed successfully")
        } catch (e: Exception) {
            logger.error("Code generation failed", e)
            throw e
        }
    }
}