package com.codingfeline.openapikotlin.runtime.serialization

import kotlinx.serialization.SerialName
import kotlin.reflect.full.findAnnotation

/**
 * Utility for working with serializable enums
 * 
 * Note: For OpenAPI-generated enums, we rely on @SerialName annotations
 * which is the standard approach in kotlinx.serialization
 */
object EnumSerializer {
    
    /**
     * Finds enum value by its serial name
     */
    inline fun <reified T : Enum<T>> fromSerialName(serialName: String): T? {
        return enumValues<T>().firstOrNull { enumValue ->
            val annotation = enumValue::class.findAnnotation<SerialName>()
            annotation?.value == serialName || enumValue.name == serialName
        }
    }
    
    /**
     * Gets the serial name for an enum value
     */
    inline fun <reified T : Enum<T>> toSerialName(value: T): String {
        val annotation = value::class.findAnnotation<SerialName>()
        return annotation?.value ?: value.name
    }
    
    /**
     * Creates a mapping of serial names to enum values
     */
    inline fun <reified T : Enum<T>> createSerialNameMap(): Map<String, T> {
        return enumValues<T>().associateBy { enumValue ->
            val annotation = enumValue::class.findAnnotation<SerialName>()
            annotation?.value ?: enumValue.name
        }
    }
}