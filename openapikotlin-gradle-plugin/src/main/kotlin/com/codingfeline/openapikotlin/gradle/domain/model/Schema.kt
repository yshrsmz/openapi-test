package com.codingfeline.openapikotlin.gradle.domain.model

/**
 * Domain model for OpenAPI schema
 */
data class Schema(
    val type: SchemaType? = null,
    val format: String? = null,
    val title: String? = null,
    val description: String? = null,
    val default: Any? = null,
    val nullable: Boolean = false,
    val readOnly: Boolean = false,
    val writeOnly: Boolean = false,
    val deprecated: Boolean = false,
    
    // Validation
    val required: List<String>? = null,
    val enum: List<Any>? = null,
    val minimum: Number? = null,
    val maximum: Number? = null,
    val exclusiveMinimum: Boolean = false,
    val exclusiveMaximum: Boolean = false,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: String? = null,
    val minItems: Int? = null,
    val maxItems: Int? = null,
    val uniqueItems: Boolean = false,
    
    // Object properties
    val properties: Map<String, Schema>? = null,
    val additionalProperties: Any? = null,
    
    // Array properties
    val items: Schema? = null,
    
    // Composition
    val allOf: List<Schema>? = null,
    val oneOf: List<Schema>? = null,
    val anyOf: List<Schema>? = null,
    val not: Schema? = null,
    
    // Reference
    val `$ref`: String? = null,
    
    // Discriminator
    val discriminator: Discriminator? = null
) {
    /**
     * Checks if this schema is a reference
     */
    fun isReference(): Boolean = `$ref` != null
    
    /**
     * Gets the reference name (e.g., "Pet" from "#/components/schemas/Pet")
     */
    fun getReferenceName(): String? {
        return `$ref`?.substringAfterLast('/')
    }
    
    /**
     * Checks if this schema represents an array
     */
    fun isArray(): Boolean = type == SchemaType.ARRAY
    
    /**
     * Checks if this schema represents an object
     */
    fun isObject(): Boolean = type == SchemaType.OBJECT
    
    /**
     * Checks if this schema represents a primitive type
     */
    fun isPrimitive(): Boolean = type in listOf(
        SchemaType.STRING,
        SchemaType.NUMBER,
        SchemaType.INTEGER,
        SchemaType.BOOLEAN
    )
    
    /**
     * Checks if a property is required
     */
    fun isPropertyRequired(propertyName: String): Boolean {
        return required?.contains(propertyName) ?: false
    }
}

/**
 * Schema types
 */
enum class SchemaType {
    STRING,
    NUMBER,
    INTEGER,
    BOOLEAN,
    ARRAY,
    OBJECT
}

/**
 * Discriminator for polymorphic schemas
 */
data class Discriminator(
    val propertyName: String,
    val mapping: Map<String, String>? = null
)