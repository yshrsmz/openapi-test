package com.codingfeline.openapikotlin.gradle.domain.value

/**
 * Value object representing a Kotlin type
 */
data class KotlinType(
    val simpleName: String,
    val packageName: String? = null,
    val isNullable: Boolean = false,
    val typeParameters: List<KotlinType> = emptyList()
) {
    /**
     * Gets the fully qualified name of the type
     */
    val qualifiedName: String
        get() = if (packageName != null) "$packageName.$simpleName" else simpleName
    
    /**
     * Checks if this is a primitive type
     */
    val isPrimitive: Boolean
        get() = this in primitiveTypes
    
    /**
     * Checks if this is a collection type
     */
    val isCollection: Boolean
        get() = simpleName in listOf("List", "Set", "Map", "Collection", "MutableList", "MutableSet", "MutableMap")
    
    /**
     * Gets the default value for this type
     */
    val defaultValue: String
        get() = when {
            isNullable -> "null"
            this == String -> "\"\""
            this == Int -> "0"
            this == Long -> "0L"
            this == Float -> "0.0f"
            this == Double -> "0.0"
            this == Boolean -> "false"
            simpleName == "List" -> "emptyList()"
            simpleName == "Set" -> "emptySet()"
            simpleName == "Map" -> "emptyMap()"
            else -> "null"
        }
    
    /**
     * Returns a nullable version of this type
     */
    fun nullable(): KotlinType = copy(isNullable = true)
    
    /**
     * Returns a non-nullable version of this type
     */
    fun nonNullable(): KotlinType = copy(isNullable = false)
    
    override fun toString(): String {
        val base = when {
            typeParameters.isEmpty() -> simpleName
            typeParameters.size == 1 -> "$simpleName<${typeParameters[0]}>"
            else -> "$simpleName<${typeParameters.joinToString(", ")}>"
        }
        return if (isNullable) "$base?" else base
    }
    
    companion object {
        // Primitive types
        val String = KotlinType("String")
        val Int = KotlinType("Int")
        val Long = KotlinType("Long")
        val Float = KotlinType("Float")
        val Double = KotlinType("Double")
        val Boolean = KotlinType("Boolean")
        val Any = KotlinType("Any")
        val Unit = KotlinType("Unit")
        
        // Date/Time types
        val Instant = KotlinType("Instant", "kotlinx.datetime")
        val LocalDate = KotlinType("LocalDate", "kotlinx.datetime")
        val LocalDateTime = KotlinType("LocalDateTime", "kotlinx.datetime")
        
        // Serialization types
        val JsonElement = KotlinType("JsonElement", "kotlinx.serialization.json")
        val JsonObject = KotlinType("JsonObject", "kotlinx.serialization.json")
        val JsonArray = KotlinType("JsonArray", "kotlinx.serialization.json")
        val JsonPrimitive = KotlinType("JsonPrimitive", "kotlinx.serialization.json")
        
        // Collection factories
        fun List(elementType: KotlinType) = KotlinType("List", typeParameters = listOf(elementType))
        fun Set(elementType: KotlinType) = KotlinType("Set", typeParameters = listOf(elementType))
        fun Map(keyType: KotlinType, valueType: KotlinType) = KotlinType("Map", typeParameters = listOf(keyType, valueType))
        
        private val primitiveTypes = setOf(String, Int, Long, Float, Double, Boolean)
    }
}