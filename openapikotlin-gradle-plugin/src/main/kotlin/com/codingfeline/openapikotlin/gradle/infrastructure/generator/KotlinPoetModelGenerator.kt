package com.codingfeline.openapikotlin.gradle.infrastructure.generator

import com.codingfeline.openapikotlin.gradle.ModelsConfig
import com.codingfeline.openapikotlin.gradle.domain.model.OpenApiSpec
import com.codingfeline.openapikotlin.gradle.domain.model.OperationContext
import com.codingfeline.openapikotlin.gradle.domain.model.Schema
import com.codingfeline.openapikotlin.gradle.domain.service.CodeGenerationService
import com.codingfeline.openapikotlin.gradle.domain.service.GeneratedFile
import com.codingfeline.openapikotlin.gradle.domain.value.PackageName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model generator implementation using KotlinPoet
 */
class KotlinPoetModelGenerator(
    private val typeMapper: KotlinPoetTypeMapper,
    private val config: ModelsConfig
) : CodeGenerationService {
    
    private var allSchemas: Map<String, Schema> = emptyMap()
    
    override fun generateModels(schemas: Map<String, Schema>, packageName: String): List<GeneratedFile> {
        // Store all schemas for reference resolution
        allSchemas = schemas
        
        return schemas.mapNotNull { (name, schema) ->
            // Skip generating for references or primitives without enums
            if (schema.isReference() || (schema.isPrimitive() && schema.enum == null)) {
                null
            } else {
                generateModel(name, schema, packageName)
            }
        }
    }
    
    private fun generateModel(name: String, schema: Schema, packageName: String): GeneratedFile {
        val className = ClassName(packageName, name)
        
        // Handle schema composition
        val resolvedSchema = resolveSchemaComposition(schema, packageName)
        
        val typeSpec = when {
            resolvedSchema.enum != null -> generateEnumClass(name, resolvedSchema)
            resolvedSchema.properties != null -> generateDataClass(name, resolvedSchema, packageName)
            resolvedSchema.oneOf != null -> generateSealedInterface(name, resolvedSchema, packageName)
            else -> generateTypeAlias(name, resolvedSchema)
        }
        
        val fileSpec = FileSpec.builder(packageName, name)
            .addType(typeSpec)
            .addImport("kotlinx.serialization", "Serializable", "SerialName")
            .apply {
                if (config.useKotlinxDatetime) {
                    addImport("kotlinx.datetime", "Instant", "LocalDate")
                }
            }
            .build()
        
        val relativePath = PackageName(packageName).toPath() + "/$name.kt"
        return GeneratedFile(relativePath, fileSpec.toString())
    }
    
    private fun resolveSchemaComposition(schema: Schema, packageName: String): Schema {
        return when {
            schema.allOf != null -> mergeAllOfSchemas(schema, packageName)
            else -> schema
        }
    }
    
    private fun mergeAllOfSchemas(schema: Schema, packageName: String): Schema {
        // Collect all properties from all schemas
        val mergedProperties = mutableMapOf<String, Schema>()
        val mergedRequired = mutableSetOf<String>()
        
        schema.allOf?.forEach { subSchema ->
            val resolved = resolveSchemaReference(subSchema)
            
            resolved.properties?.forEach { (propName, propSchema) ->
                mergedProperties[propName] = propSchema
            }
            
            resolved.required?.forEach { requiredProp ->
                mergedRequired.add(requiredProp)
            }
        }
        
        // If the main schema has its own properties, add them too
        schema.properties?.forEach { (propName, propSchema) ->
            mergedProperties[propName] = propSchema
        }
        
        schema.required?.forEach { requiredProp ->
            mergedRequired.add(requiredProp)
        }
        
        return schema.copy(
            properties = mergedProperties,
            required = mergedRequired.toList(),
            allOf = null // Clear allOf since we've merged everything
        )
    }
    
    private fun resolveSchemaReference(schema: Schema): Schema {
        return if (schema.isReference()) {
            val refName = schema.getReferenceName()
            allSchemas[refName] ?: schema
        } else {
            schema
        }
    }
    
    private fun generateSealedInterface(name: String, schema: Schema, packageName: String): TypeSpec {
        // TODO: Implement oneOf as sealed interface
        return TypeSpec.interfaceBuilder(name)
            .addModifiers(KModifier.SEALED)
            .addAnnotation(Serializable::class)
            .apply {
                if (schema.description != null) {
                    addKdoc(schema.description)
                }
            }
            .build()
    }
    
    private fun generateEnumClass(name: String, schema: Schema): TypeSpec {
        val enumBuilder = TypeSpec.enumBuilder(name)
            .addAnnotation(Serializable::class)
        
        schema.enum?.forEach { value ->
            val enumName = value.toString()
                .uppercase()
                .replace("-", "_")
                .replace(" ", "_")
            
            enumBuilder.addEnumConstant(
                enumName,
                TypeSpec.anonymousClassBuilder()
                    .addAnnotation(
                        AnnotationSpec.builder(SerialName::class)
                            .addMember("%S", value.toString())
                            .build()
                    )
                    .build()
            )
        }
        
        return enumBuilder.build()
    }
    
    private fun generateDataClass(name: String, schema: Schema, packageName: String): TypeSpec {
        val classBuilder = TypeSpec.classBuilder(name)
            .addModifiers(KModifier.DATA)
            .addAnnotation(Serializable::class)
        
        if (schema.description != null) {
            classBuilder.addKdoc(schema.description)
        }
        
        val constructor = FunSpec.constructorBuilder()
        val properties = schema.properties ?: emptyMap()
        
        properties.forEach { (propName, propSchema) ->
            val propertyName = propName.toCamelCase()
            val isRequired = schema.isPropertyRequired(propName)
            val kotlinType = typeMapper.mapType(propSchema, !isRequired)
            val typeName = kotlinType.toTypeName()
            
            val parameter = ParameterSpec.builder(propertyName, typeName)
            
            // Add default value for optional properties
            if (!isRequired && config.generateDefaultValues) {
                val defaultValue = typeMapper.getDefaultValue(kotlinType)
                if (defaultValue != null) {
                    parameter.defaultValue(defaultValue)
                }
            }
            
            // Add SerialName if property name differs from Kotlin name
            if (propName != propertyName) {
                parameter.addAnnotation(
                    AnnotationSpec.builder(SerialName::class)
                        .addMember("%S", propName)
                        .build()
                )
            }
            
            constructor.addParameter(parameter.build())
            
            val property = PropertySpec.builder(propertyName, typeName)
                .initializer(propertyName)
                .apply {
                    if (propSchema.description != null) {
                        addKdoc(propSchema.description)
                    }
                    // SerialName annotation is already added to the constructor parameter
                    // No need to add it again to the property
                }
                .build()
            
            classBuilder.addProperty(property)
        }
        
        classBuilder.primaryConstructor(constructor.build())
        
        return classBuilder.build()
    }
    
    private fun generateTypeAlias(name: String, schema: Schema): TypeSpec {
        // For now, just create a simple wrapper class
        return TypeSpec.classBuilder(name)
            .addAnnotation(Serializable::class)
            .addKdoc("Type alias for ${schema.type ?: "unknown type"}")
            .build()
    }
    
    override fun generateClient(spec: OpenApiSpec, operations: List<OperationContext>, packageName: String): GeneratedFile {
        // Model generator doesn't generate clients
        throw UnsupportedOperationException("Model generator does not generate clients")
    }
    
    override fun generateAuthHelpers(spec: OpenApiSpec, packageName: String): List<GeneratedFile> {
        // Model generator doesn't generate auth helpers
        return emptyList()
    }
    
    private fun com.codingfeline.openapikotlin.gradle.domain.value.KotlinType.toTypeName(): TypeName {
        val baseType = when {
            packageName != null -> ClassName(packageName, simpleName)
            simpleName == "List" && typeParameters.size == 1 -> 
                LIST.parameterizedBy(typeParameters[0].toTypeName())
            simpleName == "Map" && typeParameters.size == 2 ->
                MAP.parameterizedBy(
                    typeParameters[0].toTypeName(),
                    typeParameters[1].toTypeName()
                )
            else -> when (simpleName) {
                "String" -> STRING
                "Int" -> INT
                "Long" -> LONG
                "Float" -> FLOAT
                "Double" -> DOUBLE
                "Boolean" -> BOOLEAN
                "Any" -> ANY
                else -> ClassName("kotlin", simpleName)
            }
        }
        
        return if (isNullable) baseType.copy(nullable = true) else baseType
    }
    
    private fun String.toCamelCase(): String {
        // Handle empty or invalid property names
        if (this.isBlank()) return "property"
        
        // If it's already camelCase and doesn't contain special characters, return as-is but ensure first char is lowercase
        if (this.matches(Regex("^[a-zA-Z][a-zA-Z0-9]*$"))) {
            return this.replaceFirstChar { it.lowercase() }
        }
        
        // Handle special characters by replacing them with underscores
        val sanitized = this.replace(".", "_")
            .replace(" ", "_")
            .replace("-", "_")
            .replace("/", "_")
            .replace("\\", "_")
            .replace("(", "_")
            .replace(")", "_")
            .replace("[", "_")
            .replace("]", "_")
            .replace("{", "_")
            .replace("}", "_")
            .replace("#", "_")
            .replace("@", "_")
            .replace("!", "_")
            .replace("$", "_")
            .replace("%", "_")
            .replace("^", "_")
            .replace("&", "_")
            .replace("*", "_")
            .replace("+", "_")
            .replace("=", "_")
            .replace("|", "_")
            .replace(":", "_")
            .replace(";", "_")
            .replace("'", "_")
            .replace("\"", "_")
            .replace("<", "_")
            .replace(">", "_")
            .replace(",", "_")
            .replace("?", "_")
            .replace("`", "_")
            .replace("~", "_")
        
        // Convert to camelCase
        val camelCase = sanitized.split("_")
            .filter { it.isNotEmpty() }
            .mapIndexed { index, part ->
                if (index == 0) part.lowercase()
                else part.lowercase().replaceFirstChar { it.uppercase() }
            }
            .joinToString("")
        
        // Handle reserved keywords
        return when (camelCase) {
            // Kotlin reserved keywords
            "abstract", "annotation", "as", "break", "by", "catch", "class",
            "companion", "const", "constructor", "continue", "crossinline",
            "data", "delegate", "do", "dynamic", "else", "enum", "expect",
            "external", "false", "field", "file", "final", "finally", "for",
            "fun", "get", "if", "import", "in", "infix", "init", "inline",
            "inner", "interface", "internal", "is", "it", "lateinit", "noinline",
            "null", "object", "open", "operator", "out", "override", "package",
            "param", "private", "property", "protected", "public", "receiver",
            "reified", "return", "sealed", "set", "super", "suspend", "tailrec",
            "this", "throw", "true", "try", "typealias", "typeof", "val",
            "value", "var", "vararg", "when", "where", "while" -> "`$camelCase`"
            // Also handle empty result
            "" -> "property"
            else -> camelCase
        }
    }
}