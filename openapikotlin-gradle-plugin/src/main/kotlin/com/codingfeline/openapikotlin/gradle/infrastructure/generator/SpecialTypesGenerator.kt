package com.codingfeline.openapikotlin.gradle.infrastructure.generator

import com.codingfeline.openapikotlin.gradle.domain.model.OpenApiSpec
import com.codingfeline.openapikotlin.gradle.domain.service.CodeGenerationService
import com.codingfeline.openapikotlin.gradle.domain.service.GeneratedFile
import com.squareup.kotlinpoet.*

/**
 * Generates special type definitions that are referenced but not defined in some OpenAPI specs.
 * This is common with Go-generated specs that use custom types.
 */
class SpecialTypesGenerator {
    
    // Known special types that appear in Ory and similar Go-generated specs
    private val specialTypes = mapOf(
        // Time-related types
        "Time" to INSTANT,
        "NullTime" to INSTANT.copy(nullable = true),
        "nullTime" to INSTANT.copy(nullable = true),
        
        // UUID types
        "UUID" to STRING,
        "NullUUID" to STRING.copy(nullable = true),
        "nullUUID" to STRING.copy(nullable = true),
        
        // Duration types
        "Duration" to STRING,
        "NullDuration" to STRING.copy(nullable = true),
        "nullDuration" to STRING.copy(nullable = true),
        
        // Numeric types
        "Int64" to LONG,
        "nullInt64" to LONG.copy(nullable = true),
        "NullInt64" to LONG.copy(nullable = true),
        
        // Other types
        "AmountInCent" to LONG,
        "CodeChannel" to STRING,
        "webAuthnJavaScript" to STRING,
        "checkOplSyntaxBody" to ANY,
        "courierMessageStatus" to STRING,
        "courierMessageType" to STRING,
        "selfServiceFlowType" to STRING,
        "authenticatorAssuranceLevel" to STRING,
        "InvoiceStatus" to STRING,
        "CustomHostnameStatus" to STRING
    )
    
    companion object {
        private val INSTANT = ClassName("kotlinx.datetime", "Instant")
        private val STRING = ClassName("kotlin", "String")
        private val LONG = ClassName("kotlin", "Long")
        private val ANY = ClassName("kotlin", "Any")
    }
    
    fun generateModels(
        spec: OpenApiSpec,
        packageName: String
    ): List<GeneratedFile> {
        // Check if any special types are referenced in the spec
        val referencedTypes = mutableSetOf<String>()
        
        // Check all schema references
        spec.paths.values.forEach { pathItem ->
            val operations = listOfNotNull(
                pathItem.get, pathItem.post, pathItem.put, 
                pathItem.delete, pathItem.patch, pathItem.head, 
                pathItem.options
            )
            
            operations.forEach { operation ->
                // Check parameters
                operation.parameters?.forEach { param ->
                    param.schema?.`$ref`?.let { ref ->
                        val typeName = ref.substringAfterLast("/")
                        if (typeName in specialTypes) {
                            referencedTypes.add(typeName)
                        }
                    }
                }
                
                // Check request/response bodies
                operation.requestBody?.content?.values?.forEach { mediaType ->
                    collectReferencedTypes(mediaType.schema, referencedTypes)
                }
                
                operation.responses.values.forEach { response ->
                    response.content?.values?.forEach { mediaType ->
                        collectReferencedTypes(mediaType.schema, referencedTypes)
                    }
                }
            }
        }
        
        // Check component schemas
        spec.components?.schemas?.values?.forEach { schema ->
            collectReferencedTypes(schema, referencedTypes)
        }
        
        // Generate type aliases for referenced special types
        if (referencedTypes.isEmpty()) {
            return emptyList()
        }
        
        val fileSpec = FileSpec.builder(packageName, "SpecialTypes")
            .addFileComment("Special type definitions for types referenced but not defined in the OpenAPI spec")
            .apply {
                referencedTypes.forEach { typeName ->
                    specialTypes[typeName]?.let { kotlinType ->
                        addTypeAlias(
                            TypeAliasSpec.builder(typeName, kotlinType)
                                .build()
                        )
                    }
                }
            }
            .build()
            
        val relativePath = packageName.replace(".", "/") + "/SpecialTypes.kt"
        return listOf(
            GeneratedFile(relativePath, fileSpec.toString())
        )
    }
    
    private fun collectReferencedTypes(schema: com.codingfeline.openapikotlin.gradle.domain.model.Schema?, types: MutableSet<String>) {
        if (schema == null) return
        
        schema.`$ref`?.let { ref ->
            val typeName = ref.substringAfterLast("/")
            if (typeName in specialTypes) {
                types.add(typeName)
            }
        }
        
        schema.items?.let { collectReferencedTypes(it, types) }
        schema.properties?.values?.forEach { collectReferencedTypes(it, types) }
        schema.allOf?.forEach { collectReferencedTypes(it, types) }
        schema.oneOf?.forEach { collectReferencedTypes(it, types) }
        schema.anyOf?.forEach { collectReferencedTypes(it, types) }
        
        if (schema.additionalProperties is com.codingfeline.openapikotlin.gradle.domain.model.Schema) {
            collectReferencedTypes(schema.additionalProperties as com.codingfeline.openapikotlin.gradle.domain.model.Schema, types)
        }
    }
}