package com.codingfeline.openapikotlin.gradle

import com.codingfeline.openapikotlin.gradle.domain.model.Schema
import com.codingfeline.openapikotlin.gradle.domain.model.SchemaType
import com.codingfeline.openapikotlin.gradle.domain.value.KotlinType
import com.codingfeline.openapikotlin.gradle.infrastructure.generator.KotlinPoetTypeMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DynamicSchemaHandlingTest {
    
    @Test
    fun `untyped schema maps to Any by default with warning`() {
        val mapper = KotlinPoetTypeMapper("com.example")
        val schema = Schema(type = null)
        
        val result = mapper.mapType(schema, false)
        
        assertEquals(KotlinType.Any, result)
    }
    
    @Test
    fun `untyped schema fails when configured to fail`() {
        val config = ModelsConfig(
            dynamicTypeHandling = DynamicTypeHandling.FAIL
        )
        val mapper = KotlinPoetTypeMapper("com.example", config)
        val schema = Schema(type = null)
        
        assertThrows<IllegalStateException> {
            mapper.mapType(schema, false)
        }
    }
    
    @Test
    fun `untyped schema maps to JsonElement when configured`() {
        val config = ModelsConfig(useJsonElementForDynamicTypes = true)
        val mapper = KotlinPoetTypeMapper("com.example", config)
        val schema = Schema(type = null)
        
        val result = mapper.mapType(schema, false)
        
        assertEquals(KotlinType.JsonElement, result)
    }
    
    @Test
    fun `smart type inference for object-like schemas`() {
        val config = ModelsConfig(useJsonElementForDynamicTypes = true)
        val mapper = KotlinPoetTypeMapper("com.example", config)
        
        // Schema with properties should infer JsonObject
        val objectSchema = Schema(type = null, properties = mapOf("id" to Schema()))
        assertEquals(KotlinType.JsonObject, mapper.mapType(objectSchema, false))
        
        // Schema with additionalProperties should infer JsonObject
        val additionalPropsSchema = Schema(type = null, additionalProperties = true)
        assertEquals(KotlinType.JsonObject, mapper.mapType(additionalPropsSchema, false))
        
        // Schema with items should infer JsonArray
        val arraySchema = Schema(type = null, items = Schema())
        assertEquals(KotlinType.JsonArray, mapper.mapType(arraySchema, false))
        
        // Schema with no hints should use JsonElement
        val dynamicSchema = Schema(type = null)
        assertEquals(KotlinType.JsonElement, mapper.mapType(dynamicSchema, false))
    }
    
    @Test
    fun `nullable untyped schema is properly handled`() {
        val config = ModelsConfig(useJsonElementForDynamicTypes = true)
        val mapper = KotlinPoetTypeMapper("com.example", config)
        val schema = Schema(type = null, nullable = true)
        
        val result = mapper.mapType(schema, false)
        
        assertEquals(KotlinType.JsonElement.nullable(), result)
        assertTrue(result.isNullable)
    }
    
    @Test
    fun `reference schemas are not treated as untyped`() {
        val mapper = KotlinPoetTypeMapper("com.example")
        val schema = Schema(`$ref` = "#/components/schemas/SomeType")
        
        val result = mapper.mapType(schema, false)
        
        assertEquals("SomeType", result.simpleName)
        assertEquals("com.example.models", result.packageName)
    }
    
    @Test
    fun `schema type override takes precedence`() {
        val config = ModelsConfig(
            schemaTypeOverrides = mapOf("Custom" to "JsonObject")
        )
        val mapper = KotlinPoetTypeMapper("com.example", config)
        val schema = Schema(type = null)
        
        val result = mapper.mapTypeWithName(schema, false, "Custom")
        
        assertEquals(KotlinType.JsonObject, result)
    }
    
    @Test
    fun `schema type override supports custom types`() {
        val config = ModelsConfig(
            schemaTypeOverrides = mapOf(
                "CustomData" to "com.example.CustomData"
            )
        )
        val mapper = KotlinPoetTypeMapper("com.example", config)
        val schema = Schema(type = null)
        
        val result = mapper.mapTypeWithName(schema, false, "CustomData")
        
        assertEquals("CustomData", result.simpleName)
        assertEquals("com.example", result.packageName)
    }
    
    @Test
    fun `test type override parsing for all JSON types`() {
        val config = ModelsConfig(
            schemaTypeOverrides = mapOf(
                "identityTraits" to "JsonElement",
                "metadata" to "JsonObject",
                "data" to "JsonArray",
                "primitive" to "JsonPrimitive",
                "map" to "Map<String, JsonElement>"
            )
        )
        val mapper = KotlinPoetTypeMapper("com.example", config)
        
        // Test each override
        val schema = Schema(type = null)
        
        assertEquals(KotlinType.JsonElement, mapper.mapTypeWithName(schema, false, "identityTraits"))
        assertEquals(KotlinType.JsonObject, mapper.mapTypeWithName(schema, false, "metadata"))
        assertEquals(KotlinType.JsonArray, mapper.mapTypeWithName(schema, false, "data"))
        assertEquals(KotlinType.JsonPrimitive, mapper.mapTypeWithName(schema, false, "primitive"))
        
        val mapResult = mapper.mapTypeWithName(schema, false, "map")
        assertEquals("Map", mapResult.simpleName)
        assertEquals(2, mapResult.typeParameters.size)
        assertEquals(KotlinType.String, mapResult.typeParameters[0])
        assertEquals(KotlinType.JsonElement, mapResult.typeParameters[1])
    }
}