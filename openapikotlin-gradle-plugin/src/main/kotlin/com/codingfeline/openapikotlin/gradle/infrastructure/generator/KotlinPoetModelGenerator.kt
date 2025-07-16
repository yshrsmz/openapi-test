package com.codingfeline.openapikotlin.gradle.infrastructure.generator

import com.codingfeline.openapikotlin.gradle.ModelsConfig
import com.codingfeline.openapikotlin.gradle.domain.model.OpenApiSpec
import com.codingfeline.openapikotlin.gradle.domain.model.OperationContext
import com.codingfeline.openapikotlin.gradle.domain.model.Schema
import com.codingfeline.openapikotlin.gradle.domain.service.CodeGenerationService
import com.codingfeline.openapikotlin.gradle.domain.service.GeneratedFile
import com.codingfeline.openapikotlin.gradle.domain.value.PackageName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model generator implementation using KotlinPoet
 */
class KotlinPoetModelGenerator(
    private val typeMapper: KotlinPoetTypeMapper,
    private val config: ModelsConfig
) : CodeGenerationService {
    
    override fun generateModels(schemas: Map<String, Schema>, packageName: String): List<GeneratedFile> {
        return schemas.mapNotNull { (name, schema) ->
            // Skip generating for primitive schemas or references
            if (schema.isReference() || schema.isPrimitive()) {
                null
            } else {
                generateModel(name, schema, packageName)
            }
        }
    }
    
    private fun generateModel(name: String, schema: Schema, packageName: String): GeneratedFile {
        val className = ClassName(packageName, name)
        
        val typeSpec = when {
            schema.enum != null -> generateEnumClass(name, schema)
            schema.properties != null -> generateDataClass(name, schema, packageName)
            else -> generateTypeAlias(name, schema)
        }
        
        val fileSpec = FileSpec.builder(packageName, name)
            .addType(typeSpec)
            .addImport("kotlinx.serialization", "Serializable", "SerialName")
            .apply {
                if (config.useKotlinxDatetime) {
                    addImport("kotlinx.datetime", "Instant", "LocalDate")
                }
            }
            .build()
        
        val relativePath = PackageName(packageName).toPath() + "/$name.kt"
        return GeneratedFile(relativePath, fileSpec.toString())
    }
    
    private fun generateEnumClass(name: String, schema: Schema): TypeSpec {
        val enumBuilder = TypeSpec.enumBuilder(name)
            .addAnnotation(Serializable::class)
        
        schema.enum?.forEach { value ->
            val enumName = value.toString()
                .uppercase()
                .replace("-", "_")
                .replace(" ", "_")
            
            enumBuilder.addEnumConstant(
                enumName,
                TypeSpec.anonymousClassBuilder()
                    .addAnnotation(
                        AnnotationSpec.builder(SerialName::class)
                            .addMember("%S", value.toString())
                            .build()
                    )
                    .build()
            )
        }
        
        return enumBuilder.build()
    }
    
    private fun generateDataClass(name: String, schema: Schema, packageName: String): TypeSpec {
        val classBuilder = TypeSpec.classBuilder(name)
            .addModifiers(KModifier.DATA)
            .addAnnotation(Serializable::class)
        
        if (schema.description != null) {
            classBuilder.addKdoc(schema.description)
        }
        
        val constructor = FunSpec.constructorBuilder()
        val properties = schema.properties ?: emptyMap()
        
        properties.forEach { (propName, propSchema) ->
            val propertyName = propName.toCamelCase()
            val isRequired = schema.isPropertyRequired(propName)
            val kotlinType = typeMapper.mapType(propSchema, !isRequired)
            val typeName = kotlinType.toTypeName()
            
            val parameter = ParameterSpec.builder(propertyName, typeName)
            
            // Add default value for optional properties
            if (!isRequired && config.generateDefaultValues) {
                val defaultValue = typeMapper.getDefaultValue(kotlinType)
                if (defaultValue != null) {
                    parameter.defaultValue(defaultValue)
                }
            }
            
            // Add SerialName if property name differs from Kotlin name
            if (propName != propertyName) {
                parameter.addAnnotation(
                    AnnotationSpec.builder(SerialName::class)
                        .addMember("%S", propName)
                        .build()
                )
            }
            
            constructor.addParameter(parameter.build())
            
            val property = PropertySpec.builder(propertyName, typeName)
                .initializer(propertyName)
                .apply {
                    if (propSchema.description != null) {
                        addKdoc(propSchema.description)
                    }
                    if (propName != propertyName) {
                        addAnnotation(
                            AnnotationSpec.builder(SerialName::class)
                                .addMember("%S", propName)
                                .build()
                        )
                    }
                }
                .build()
            
            classBuilder.addProperty(property)
        }
        
        classBuilder.primaryConstructor(constructor.build())
        
        return classBuilder.build()
    }
    
    private fun generateTypeAlias(name: String, schema: Schema): TypeSpec {
        // For now, just create a simple wrapper class
        return TypeSpec.classBuilder(name)
            .addAnnotation(Serializable::class)
            .addKdoc("Type alias for ${schema.type ?: "unknown type"}")
            .build()
    }
    
    override fun generateClient(spec: OpenApiSpec, operations: List<OperationContext>, packageName: String): GeneratedFile {
        // Model generator doesn't generate clients
        throw UnsupportedOperationException("Model generator does not generate clients")
    }
    
    override fun generateAuthHelpers(spec: OpenApiSpec, packageName: String): List<GeneratedFile> {
        // Model generator doesn't generate auth helpers
        return emptyList()
    }
    
    private fun com.codingfeline.openapikotlin.gradle.domain.value.KotlinType.toTypeName(): TypeName {
        val baseType = when {
            packageName != null -> ClassName(packageName, simpleName)
            simpleName == "List" && typeParameters.size == 1 -> 
                LIST.parameterizedBy(typeParameters[0].toTypeName())
            simpleName == "Map" && typeParameters.size == 2 ->
                MAP.parameterizedBy(
                    typeParameters[0].toTypeName(),
                    typeParameters[1].toTypeName()
                )
            else -> when (simpleName) {
                "String" -> STRING
                "Int" -> INT
                "Long" -> LONG
                "Float" -> FLOAT
                "Double" -> DOUBLE
                "Boolean" -> BOOLEAN
                "Any" -> ANY
                else -> ClassName("kotlin", simpleName)
            }
        }
        
        return if (isNullable) baseType.copy(nullable = true) else baseType
    }
    
    private fun String.toCamelCase(): String {
        return split("_", "-")
            .mapIndexed { index, part ->
                if (index == 0) part.lowercase()
                else part.capitalize()
            }
            .joinToString("")
    }
}