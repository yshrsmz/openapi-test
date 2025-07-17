package com.codingfeline.openapikotlin.gradle

import com.codingfeline.openapikotlin.gradle.domain.model.Schema
import com.codingfeline.openapikotlin.gradle.domain.model.SchemaType
import com.codingfeline.openapikotlin.gradle.infrastructure.generator.KotlinPoetTypeMapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MapTypeGenerationTest {
    
    @Test
    fun `test object with additionalProperties generates correct Map type`() {
        val typeMapper = KotlinPoetTypeMapper("com.example")
        
        // Create schema for integer value
        val intSchema = Schema(
            type = SchemaType.INTEGER,
            format = "int32"
        )
        
        // Create object schema with additionalProperties
        val mapSchema = Schema(
            type = SchemaType.OBJECT,
            properties = null, // No fixed properties
            additionalProperties = intSchema
        )
        
        val result = typeMapper.mapType(mapSchema, false)
        
        assertEquals("Map", result.simpleName)
        assertEquals(2, result.typeParameters.size)
        assertEquals("String", result.typeParameters[0].simpleName)
        assertEquals("Int", result.typeParameters[1].simpleName)
    }
    
    @Test
    fun `test object with boolean true additionalProperties generates Map of JsonElement`() {
        val typeMapper = KotlinPoetTypeMapper("com.example")
        
        // Create object schema with additionalProperties = true
        val mapSchema = Schema(
            type = SchemaType.OBJECT,
            properties = null,
            additionalProperties = true
        )
        
        val result = typeMapper.mapType(mapSchema, false)
        
        assertEquals("Map", result.simpleName)
        assertEquals(2, result.typeParameters.size)
        assertEquals("String", result.typeParameters[0].simpleName)
        assertEquals("JsonElement", result.typeParameters[1].simpleName)
        assertEquals("kotlinx.serialization.json", result.typeParameters[1].packageName)
    }
    
    @Test
    fun `test object with boolean false additionalProperties generates regular object`() {
        val typeMapper = KotlinPoetTypeMapper("com.example")
        
        // Create object schema with additionalProperties = false
        val mapSchema = Schema(
            type = SchemaType.OBJECT,
            properties = null,
            additionalProperties = false
        )
        
        val result = typeMapper.mapType(mapSchema, false)
        
        // Should not generate a Map when additionalProperties is false
        assertEquals("Any", result.simpleName)
    }
}