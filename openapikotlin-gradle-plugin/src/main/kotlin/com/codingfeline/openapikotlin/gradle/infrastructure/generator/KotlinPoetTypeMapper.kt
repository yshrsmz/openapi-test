package com.codingfeline.openapikotlin.gradle.infrastructure.generator

import com.codingfeline.openapikotlin.gradle.domain.model.Schema
import com.codingfeline.openapikotlin.gradle.domain.model.SchemaType
import com.codingfeline.openapikotlin.gradle.domain.service.TypeMappingService
import com.codingfeline.openapikotlin.gradle.domain.value.KotlinType
import com.codingfeline.openapikotlin.gradle.domain.value.PackageName

/**
 * Type mapper implementation using KotlinPoet
 */
class KotlinPoetTypeMapper(val basePackage: String) : TypeMappingService {
    
    private val modelsPackage = PackageName(basePackage).append("models")
    
    override fun mapType(schema: Schema, nullable: Boolean): KotlinType {
        val baseType = when {
            schema.isReference() -> mapReferenceType(schema.getReferenceName()!!, false)
            schema.isArray() -> mapArrayType(schema.items!!, false)
            schema.isPrimitive() -> mapPrimitiveType(schema.type!!, schema.format, false)
            schema.isObject() -> {
                // If it's a simple object without properties, use Map
                if (schema.properties.isNullOrEmpty()) {
                    KotlinType.Map(KotlinType.String, KotlinType.Any)
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
}