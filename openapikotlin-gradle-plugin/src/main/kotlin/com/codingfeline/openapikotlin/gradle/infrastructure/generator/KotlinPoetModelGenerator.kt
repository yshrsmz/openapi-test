package com.codingfeline.openapikotlin.gradle.infrastructure.generator

import com.codingfeline.openapikotlin.gradle.ModelsConfig
import com.codingfeline.openapikotlin.gradle.domain.model.OpenApiSpec
import com.codingfeline.openapikotlin.gradle.domain.model.OperationContext
import com.codingfeline.openapikotlin.gradle.domain.model.Schema
import com.codingfeline.openapikotlin.gradle.domain.model.SchemaType
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
    private val interfaceImplementations = mutableMapOf<String, String>() // schema name -> interface name
    
    companion object {
        private val STRING = ClassName("kotlin", "String")
        private val INT = ClassName("kotlin", "Int")
        private val LONG = ClassName("kotlin", "Long")
        private val FLOAT = ClassName("kotlin", "Float")
        private val DOUBLE = ClassName("kotlin", "Double")
        private val BOOLEAN = ClassName("kotlin", "Boolean")
        private val ANY = ClassName("kotlin", "Any")
        private val LIST = ClassName("kotlin.collections", "List")
        private val MAP = ClassName("kotlin.collections", "Map")
        private val INSTANT = ClassName("kotlinx.datetime", "Instant")
        private val LOCAL_DATE = ClassName("kotlinx.datetime", "LocalDate")
    }
    
    override fun generateModels(schemas: Map<String, Schema>, packageName: String): List<GeneratedFile> {
        // Store all schemas for reference resolution
        allSchemas = schemas
        
        // First pass: identify oneOf relationships
        schemas.forEach { (name, schema) ->
            if (schema.oneOf != null) {
                schema.oneOf.forEach { oneOfSchema ->
                    if (oneOfSchema.isReference()) {
                        val refName = oneOfSchema.getReferenceName()
                        if (refName != null) {
                            interfaceImplementations[refName] = name
                        }
                    }
                }
            }
        }
        
        val models = mutableListOf<GeneratedFile>()
        
        // Second pass: generate models
        schemas.forEach { (name, schema) ->
            // Skip generating for references
            if (!schema.isReference()) {
                models.add(generateModel(name, schema, packageName))
                
                // Also generate enums for properties with enum values
                val nestedEnums = generateNestedEnums(name, schema, packageName)
                models.addAll(nestedEnums)
            }
        }
        
        return models
    }
    
    private fun generateNestedEnums(parentName: String, schema: Schema, packageName: String): List<GeneratedFile> {
        val enums = mutableListOf<GeneratedFile>()
        val resolvedSchema = resolveSchemaComposition(schema, packageName)
        
        resolvedSchema.properties?.forEach { (propName, propSchema) ->
            if (propSchema.enum != null && propSchema.type == SchemaType.STRING) {
                val enumName = "${parentName}${propName.replaceFirstChar { it.uppercase() }}"
                enums.add(generateModel(enumName, propSchema, packageName))
            }
        }
        
        return enums
    }
    
    private fun generateModel(name: String, schema: Schema, packageName: String): GeneratedFile {
        val className = ClassName(packageName, name)
        
        // Handle schema composition
        val resolvedSchema = resolveSchemaComposition(schema, packageName)
        
        // Check if this is a simple type that should be a type alias
        val isSimpleType = resolvedSchema.enum == null && 
                          resolvedSchema.properties == null && 
                          resolvedSchema.oneOf == null && 
                          resolvedSchema.anyOf == null
        
        if (isSimpleType) {
            // Generate type alias for simple types
            val kotlinType = when {
                resolvedSchema.type == SchemaType.STRING && resolvedSchema.format == "date-time" -> INSTANT
                resolvedSchema.type == SchemaType.STRING && resolvedSchema.format == "date" -> LOCAL_DATE
                resolvedSchema.type == SchemaType.STRING -> STRING
                resolvedSchema.type == SchemaType.INTEGER && resolvedSchema.format == "int64" -> LONG
                resolvedSchema.type == SchemaType.INTEGER -> INT
                resolvedSchema.type == SchemaType.NUMBER && resolvedSchema.format == "double" -> DOUBLE
                resolvedSchema.type == SchemaType.NUMBER -> FLOAT
                resolvedSchema.type == SchemaType.BOOLEAN -> BOOLEAN
                resolvedSchema.type == SchemaType.ARRAY -> LIST.parameterizedBy(ANY)
                resolvedSchema.type == SchemaType.OBJECT -> MAP.parameterizedBy(STRING, ANY)
                else -> ANY
            }.copy(nullable = resolvedSchema.nullable == true)
            
            val fileSpec = FileSpec.builder(packageName, name)
                .addTypeAlias(TypeAliasSpec.builder(name, kotlinType).build())
                .apply {
                    if (kotlinType == INSTANT || kotlinType == LOCAL_DATE || 
                        kotlinType.copy(nullable = false) == INSTANT || 
                        kotlinType.copy(nullable = false) == LOCAL_DATE) {
                        addImport("kotlinx.datetime", "Instant", "LocalDate")
                    }
                }
                .build()
            
            val relativePath = PackageName(packageName).toPath() + "/$name.kt"
            return GeneratedFile(relativePath, fileSpec.toString())
        }
        
        // Generate regular type (enum, data class, interface, etc.)
        val typeSpec = when {
            resolvedSchema.enum != null -> generateEnumClass(name, resolvedSchema)
            resolvedSchema.properties != null -> generateDataClass(name, resolvedSchema, packageName)
            resolvedSchema.oneOf != null -> generateSealedInterface(name, resolvedSchema, packageName)
            resolvedSchema.anyOf != null -> generateAnyOfClass(name, resolvedSchema, packageName)
            else -> throw IllegalStateException("Unexpected schema type for $name")
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
            schema.oneOf != null -> schema // oneOf is handled differently in generateModel
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
            val resolved = allSchemas[refName] ?: schema
            // If the resolved schema also has allOf, we need to resolve it recursively
            if (resolved.allOf != null) {
                mergeAllOfSchemas(resolved, "")
            } else {
                resolved
            }
        } else {
            schema
        }
    }
    
    private fun generateSealedInterface(name: String, schema: Schema, packageName: String): TypeSpec {
        val interfaceBuilder = TypeSpec.interfaceBuilder(name)
            .addModifiers(KModifier.SEALED)
            .addAnnotation(Serializable::class)
            .apply {
                if (schema.description != null) {
                    addKdoc(schema.description)
                }
            }
        
        // If there's a discriminator, add it as a property
        schema.discriminator?.let { discriminator ->
            val propertySpec = PropertySpec.builder(
                discriminator.propertyName.toCamelCase(),
                STRING
            )
                .addModifiers(KModifier.ABSTRACT)
                .build()
            interfaceBuilder.addProperty(propertySpec)
        }
        
        return interfaceBuilder.build()
    }
    
    private fun generateAnyOfClass(name: String, schema: Schema, packageName: String): TypeSpec {
        // For anyOf, we generate a simple data class with a value of type Any
        // In a real implementation, we'd need a custom serializer
        return TypeSpec.classBuilder(name)
            .addModifiers(KModifier.DATA)
            .addAnnotation(Serializable::class)
            .apply {
                if (schema.description != null) {
                    addKdoc(schema.description)
                }
            }
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("value", ANY)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("value", ANY)
                    .initializer("value")
                    .build()
            )
            .build()
    }
    
    private fun generateEnumClass(name: String, schema: Schema): TypeSpec {
        val enumBuilder = TypeSpec.enumBuilder(name)
            .addAnnotation(Serializable::class)
        
        schema.enum?.forEach { value ->
            val rawValue = value.toString()
            val enumName = if (rawValue.isEmpty()) {
                "EMPTY"
            } else {
                rawValue
                    .uppercase()
                    .replace("-", "_")
                    .replace(" ", "_")
                    .replace(".", "_")
                    .let { if (it.first().isDigit()) "_$it" else it }
            }
            
            enumBuilder.addEnumConstant(
                enumName,
                TypeSpec.anonymousClassBuilder()
                    .addAnnotation(
                        AnnotationSpec.builder(SerialName::class)
                            .addMember("%S", rawValue)
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
        
        // Check if this class should implement a oneOf interface
        interfaceImplementations[name]?.let { interfaceName ->
            classBuilder.addSuperinterface(ClassName(packageName, interfaceName))
        }
        
        val constructor = FunSpec.constructorBuilder()
        val properties = schema.properties ?: emptyMap()
        
        properties.forEach { (propName, propSchema) ->
            val propertyName = propName.toCamelCase()
            val isRequired = schema.isPropertyRequired(propName)
            
            // If property has enum values, use the generated enum type
            val typeName = if (propSchema.enum != null && propSchema.type == SchemaType.STRING) {
                val enumName = "${name}${propName.replaceFirstChar { it.uppercase() }}"
                ClassName(packageName, enumName).copy(nullable = !isRequired)
            } else {
                val kotlinType = typeMapper.mapType(propSchema, !isRequired)
                kotlinType.toTypeName()
            }
            
            val parameter = ParameterSpec.builder(propertyName, typeName)
            
            // Add default value for optional properties
            if (!isRequired && config.generateDefaultValues) {
                if (propSchema.enum != null) {
                    // For enums, default to null
                    parameter.defaultValue("null")
                } else {
                    val kotlinType = typeMapper.mapType(propSchema, !isRequired)
                    val defaultValue = typeMapper.getDefaultValue(kotlinType)
                    if (defaultValue != null) {
                        parameter.defaultValue(defaultValue)
                    }
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
                    // Add override modifier if this property is defined in the interface
                    interfaceImplementations[name]?.let { interfaceName ->
                        // Check if the interface has this property
                        val interfaceSchema = allSchemas[interfaceName]
                        if (interfaceSchema?.properties?.containsKey(propName) == true) {
                            addModifiers(KModifier.OVERRIDE)
                        }
                        // Also check if this is a discriminator property
                        if (interfaceSchema?.discriminator?.propertyName == propName) {
                            addModifiers(KModifier.OVERRIDE)
                        }
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