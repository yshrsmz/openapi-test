package com.codingfeline.openapikotlin.gradle.domain.service

import com.codingfeline.openapikotlin.gradle.domain.model.Schema
import com.codingfeline.openapikotlin.gradle.domain.model.SchemaType
import com.codingfeline.openapikotlin.gradle.domain.value.KotlinType

/**
 * Domain service for mapping OpenAPI types to Kotlin types
 */
interface TypeMappingService {
    
    /**
     * Maps an OpenAPI schema to a Kotlin type
     */
    fun mapType(schema: Schema, nullable: Boolean = false): KotlinType
    
    /**
     * Maps a primitive schema type to Kotlin type
     */
    fun mapPrimitiveType(
        type: SchemaType,
        format: String? = null,
        nullable: Boolean = false
    ): KotlinType
    
    /**
     * Gets the Kotlin type for a schema reference
     */
    fun mapReferenceType(
        referenceName: String,
        nullable: Boolean = false
    ): KotlinType
    
    /**
     * Maps an array schema to a Kotlin collection type
     */
    fun mapArrayType(
        itemSchema: Schema,
        nullable: Boolean = false
    ): KotlinType
    
    /**
     * Maps an object schema to a Kotlin map or custom type
     */
    fun mapObjectType(
        schema: Schema,
        schemaName: String? = null,
        nullable: Boolean = false
    ): KotlinType
    
    /**
     * Gets the default value for a Kotlin type
     */
    fun getDefaultValue(type: KotlinType): String?
}