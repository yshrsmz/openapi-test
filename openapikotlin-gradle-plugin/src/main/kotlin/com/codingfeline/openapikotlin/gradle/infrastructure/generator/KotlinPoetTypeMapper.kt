package com.codingfeline.openapikotlin.gradle.infrastructure.generator

import com.codingfeline.openapikotlin.gradle.DynamicTypeHandling
import com.codingfeline.openapikotlin.gradle.ModelsConfig
import com.codingfeline.openapikotlin.gradle.domain.model.Schema
import com.codingfeline.openapikotlin.gradle.domain.model.SchemaType
import com.codingfeline.openapikotlin.gradle.domain.service.TypeMappingService
import com.codingfeline.openapikotlin.gradle.domain.value.KotlinType
import com.codingfeline.openapikotlin.gradle.domain.value.PackageName
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Type mapper implementation using KotlinPoet
 */
class KotlinPoetTypeMapper(
    val basePackage: String,
    private val config: ModelsConfig = ModelsConfig()
) : TypeMappingService {
    
    private val logger: Logger = Logging.getLogger(KotlinPoetTypeMapper::class.java)
    
    private val modelsPackage = PackageName(basePackage).append("models")
    
    override fun mapType(schema: Schema, nullable: Boolean): KotlinType {
        return mapTypeWithName(schema, nullable, null)
    }
    
    /**
     * Maps a schema to a Kotlin type with an optional schema name for overrides
     */
    fun mapTypeWithName(schema: Schema, nullable: Boolean, schemaName: String?): KotlinType {
        // Check for schema type overrides first
        schemaName?.let { name ->
            config.schemaTypeOverrides[name]?.let { override ->
                return parseTypeOverride(override, nullable || schema.nullable)
            }
        }
        
        // Handle untyped schemas
        if (schema.type == null && !schema.isReference()) {
            return handleUntypedSchema(schema, nullable || schema.nullable, schemaName)
        }
        
        val baseType = when {
            schema.isReference() -> mapReferenceType(schema.getReferenceName()!!, false)
            schema.isArray() -> mapArrayType(schema.items!!, false)
            schema.isPrimitive() -> mapPrimitiveType(schema.type!!, schema.format, false)
            schema.isObject() -> {
                // Check if this is a Map type (object with additionalProperties)
                if (schema.properties.isNullOrEmpty() && schema.additionalProperties != null) {
                    // additionalProperties can be a boolean or a Schema
                    val valueType = when (val additionalProps = schema.additionalProperties) {
                        is Boolean -> if (additionalProps) {
                            KotlinType.JsonElement  // Use JsonElement for dynamic values
                        } else {
                            null  // additionalProperties: false means no additional properties
                        }
                        is Schema -> mapType(additionalProps, false)
                        else -> KotlinType.JsonElement
                    }
                    if (valueType != null) {
                        KotlinType.Map(KotlinType.String, valueType)
                    } else {
                        // No additional properties allowed, treat as regular object
                        KotlinType.Any
                    }
                } else if (schema.properties.isNullOrEmpty()) {
                    // Simple object without properties, use Map<String, JsonElement>
                    KotlinType.Map(KotlinType.String, KotlinType.JsonElement)
                } else {
                    // Otherwise, it should be a named type
                    KotlinType.Any
                }
            }
            else -> KotlinType.Any
        }
        
        return if (nullable || schema.nullable) baseType.nullable() else baseType
    }
    
    override fun mapPrimitiveType(type: SchemaType, format: String?, nullable: Boolean): KotlinType {
        val baseType = when (type) {
            SchemaType.STRING -> when (format) {
                "date" -> KotlinType.LocalDate
                "date-time" -> KotlinType.Instant
                "uuid" -> KotlinType.String
                "byte" -> KotlinType.String // Base64 encoded
                "binary" -> KotlinType.String
                else -> KotlinType.String
            }
            SchemaType.INTEGER -> when (format) {
                "int64" -> KotlinType.Long
                else -> KotlinType.Int
            }
            SchemaType.NUMBER -> when (format) {
                "float" -> KotlinType.Float
                else -> KotlinType.Double
            }
            SchemaType.BOOLEAN -> KotlinType.Boolean
            SchemaType.ARRAY -> throw IllegalArgumentException("Array is not a primitive type")
            SchemaType.OBJECT -> throw IllegalArgumentException("Object is not a primitive type")
        }
        
        return if (nullable) baseType.nullable() else baseType
    }
    
    override fun mapReferenceType(referenceName: String, nullable: Boolean): KotlinType {
        // References point to models in the models package
        val type = KotlinType(
            simpleName = referenceName,
            packageName = modelsPackage.value
        )
        return if (nullable) type.nullable() else type
    }
    
    override fun mapArrayType(itemSchema: Schema, nullable: Boolean): KotlinType {
        val itemType = mapType(itemSchema, false)
        val listType = KotlinType.List(itemType)
        return if (nullable) listType.nullable() else listType
    }
    
    override fun mapObjectType(schema: Schema, schemaName: String?, nullable: Boolean): KotlinType {
        return if (schemaName != null) {
            // Named object - use the schema name
            val type = KotlinType(
                simpleName = schemaName,
                packageName = modelsPackage.value
            )
            if (nullable) type.nullable() else type
        } else {
            // Anonymous object - use Map
            val mapType = KotlinType.Map(KotlinType.String, KotlinType.Any)
            if (nullable) mapType.nullable() else mapType
        }
    }
    
    override fun getDefaultValue(type: KotlinType): String? {
        return when {
            type.isNullable -> "null"
            type.isPrimitive -> type.defaultValue
            type.isCollection -> type.defaultValue
            else -> null // No default for custom types
        }
    }
    
    /**
     * Handles schemas with no type definition
     */
    private fun handleUntypedSchema(schema: Schema, nullable: Boolean, schemaName: String?): KotlinType {
        if (config.useJsonElementForDynamicTypes) {
            // Use smart type inference
            return inferDynamicType(schema).let {
                if (nullable) it.nullable() else it
            }
        } else {
            // Handle based on configured strategy
            when (config.dynamicTypeHandling) {
                DynamicTypeHandling.ALLOW -> {
                    return if (nullable) KotlinType.Any.nullable() else KotlinType.Any
                }
                
                DynamicTypeHandling.WARN -> {
                    logger.warn("""
                        Schema '${schemaName ?: "unnamed"}' has no type definition and will be mapped to 'Any'.
                        This will fail at runtime with: "Serializer has not been found for type 'Any'"
                        
                        To fix this, either:
                        1. Set useJsonElementForDynamicTypes = true in your build.gradle.kts
                        2. Add a type override: schemaTypeOverrides["${schemaName}"] = "JsonElement"
                        3. Fix the OpenAPI specification to include a type
                    """.trimIndent())
                    return if (nullable) KotlinType.Any.nullable() else KotlinType.Any
                }
                
                DynamicTypeHandling.FAIL -> {
                    throw IllegalStateException("""
                        Schema '${schemaName ?: "unnamed"}' has no type definition.
                        
                        The generated code would fail at runtime with: "Serializer has not been found for type 'Any'"
                        
                        To fix this, either:
                        1. Set useJsonElementForDynamicTypes = true in your build.gradle.kts
                        2. Add a type override: schemaTypeOverrides["${schemaName}"] = "JsonElement"
                        3. Fix the OpenAPI specification to include a type
                    """.trimIndent())
                }
            }
        }
    }
    
    /**
     * Infers the appropriate JSON type based on schema structure
     */
    private fun inferDynamicType(schema: Schema): KotlinType {
        return when {
            // If we can infer it's an object
            schema.type == SchemaType.OBJECT || 
            schema.properties != null || 
            schema.additionalProperties != null -> KotlinType.JsonObject
            
            // If we can infer it's an array
            schema.type == SchemaType.ARRAY || 
            schema.items != null -> KotlinType.JsonArray
            
            // Default to most general type
            else -> KotlinType.JsonElement
        }
    }
    
    /**
     * Parses a type override string into a KotlinType
     */
    private fun parseTypeOverride(override: String, nullable: Boolean): KotlinType {
        val type = when (override) {
            "JsonElement" -> KotlinType.JsonElement
            "JsonObject" -> KotlinType.JsonObject
            "JsonArray" -> KotlinType.JsonArray
            "JsonPrimitive" -> KotlinType.JsonPrimitive
            "Map<String, JsonElement>" -> KotlinType.Map(KotlinType.String, KotlinType.JsonElement)
            else -> {
                // Parse custom type strings like "com.example.CustomType"
                val parts = override.split(".")
                if (parts.size >= 2) {
                    val packageName = parts.dropLast(1).joinToString(".")
                    val simpleName = parts.last()
                    KotlinType(simpleName, packageName)
                } else {
                    KotlinType(override)
                }
            }
        }
        return if (nullable) type.nullable() else type
    }
}