package com.codingfeline.openapikotlin.gradle.infrastructure.generator

import com.codingfeline.openapikotlin.gradle.domain.service.TypeMappingService

/**
 * Type mapper implementation using KotlinPoet
 */
class KotlinPoetTypeMapper(private val basePackage: String) : TypeMappingService {
    // TODO: Implement type mapping
    override fun mapType(schema: com.codingfeline.openapikotlin.gradle.domain.model.Schema, nullable: Boolean): com.codingfeline.openapikotlin.gradle.domain.value.KotlinType {
        return com.codingfeline.openapikotlin.gradle.domain.value.KotlinType.String
    }
    
    override fun mapPrimitiveType(type: com.codingfeline.openapikotlin.gradle.domain.model.SchemaType, format: String?, nullable: Boolean): com.codingfeline.openapikotlin.gradle.domain.value.KotlinType {
        return com.codingfeline.openapikotlin.gradle.domain.value.KotlinType.String
    }
    
    override fun mapReferenceType(referenceName: String, nullable: Boolean): com.codingfeline.openapikotlin.gradle.domain.value.KotlinType {
        return com.codingfeline.openapikotlin.gradle.domain.value.KotlinType.String
    }
    
    override fun mapArrayType(itemSchema: com.codingfeline.openapikotlin.gradle.domain.model.Schema, nullable: Boolean): com.codingfeline.openapikotlin.gradle.domain.value.KotlinType {
        return com.codingfeline.openapikotlin.gradle.domain.value.KotlinType.List(com.codingfeline.openapikotlin.gradle.domain.value.KotlinType.String)
    }
    
    override fun mapObjectType(schema: com.codingfeline.openapikotlin.gradle.domain.model.Schema, schemaName: String?, nullable: Boolean): com.codingfeline.openapikotlin.gradle.domain.value.KotlinType {
        return com.codingfeline.openapikotlin.gradle.domain.value.KotlinType.Any
    }
    
    override fun getDefaultValue(type: com.codingfeline.openapikotlin.gradle.domain.value.KotlinType): String? {
        return type.defaultValue
    }
}