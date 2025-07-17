package com.codingfeline.openapikotlin.gradle.infrastructure.generator

import com.codingfeline.openapikotlin.gradle.ClientConfig
import com.codingfeline.openapikotlin.gradle.domain.model.*
import com.codingfeline.openapikotlin.gradle.domain.service.CodeGenerationService
import com.codingfeline.openapikotlin.gradle.domain.service.GeneratedFile
import com.codingfeline.openapikotlin.gradle.domain.value.PackageName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * Client generator implementation using KotlinPoet
 */
class KotlinPoetClientGenerator(
    private val typeMapper: KotlinPoetTypeMapper,
    private val config: ClientConfig
) : CodeGenerationService {
    
    companion object {
        private val KTOR_CLIENT = ClassName("io.ktor.client", "HttpClient")
        private val KTOR_REQUEST = ClassName("io.ktor.client.request", "HttpRequestBuilder")
        private val KTOR_RESPONSE = ClassName("io.ktor.client.statement", "HttpResponse")
        private val JSON = ClassName("kotlinx.serialization.json", "Json")
        private val OAuth2_CONFIG = ClassName("com.codingfeline.openapikotlin.runtime.auth", "OAuth2Config")
        private val OAuth2_CLIENT = ClassName("com.codingfeline.openapikotlin.runtime.auth", "OAuth2Client")
        private val TOKEN_MANAGER = ClassName("com.codingfeline.openapikotlin.runtime.auth", "TokenManager")
    }
    
    override fun generateModels(schemas: Map<String, Schema>, packageName: String): List<GeneratedFile> {
        // Client generator doesn't generate models
        return emptyList()
    }
    
    override fun generateClient(
        spec: OpenApiSpec,
        operations: List<OperationContext>,
        packageName: String
    ): GeneratedFile {
        val clientClass = generateClientClass(spec, operations, packageName)
        
        // Collect all model types used in operations
        val usedModelTypes = mutableSetOf<String>()
        operations.forEach { opContext ->
            val op = opContext.operation
            
            // Collect parameter types
            op.parameters?.forEach { param ->
                param.schema?.let { schema ->
                    collectSchemaTypes(schema, usedModelTypes)
                }
            }
            
            // Collect request body types
            op.requestBody?.content?.values?.forEach { mediaType ->
                mediaType.schema?.let { schema ->
                    collectSchemaTypes(schema, usedModelTypes)
                }
            }
            
            // Collect response types
            op.responses.values.forEach { response ->
                response.content?.values?.forEach { mediaType ->
                    mediaType.schema?.let { schema ->
                        collectSchemaTypes(schema, usedModelTypes)
                    }
                }
            }
        }
        
        val fileSpec = FileSpec.builder(packageName, config.clientClassName)
            .addType(clientClass)
            .addImports()
            .apply {
                // Add imports for all used model types
                // The packageName passed to us is already the client package,
                // so we need to go up one level and then to models
                val basePackage = packageName.substringBeforeLast(".client")
                val modelsPackage = "$basePackage.models"
                usedModelTypes.forEach { modelType ->
                    addImport(modelsPackage, modelType)
                }
            }
            .build()
        
        val relativePath = PackageName(packageName).toPath() + "/${config.clientClassName}.kt"
        return GeneratedFile(relativePath, fileSpec.toString())
    }
    
    private fun generateClientClass(
        spec: OpenApiSpec,
        operations: List<OperationContext>,
        packageName: String
    ): TypeSpec {
        val builder = TypeSpec.classBuilder(config.clientClassName)
            .addKdoc("Generated API client for ${spec.info.title}")
        
        // Add constructor
        builder.primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("httpClient", KTOR_CLIENT)
                .addParameter(
                    ParameterSpec.builder("baseUrl", String::class)
                        .defaultValue("\"%L\"", spec.servers.firstOrNull()?.url ?: "")
                        .build()
                )
                .addParameter(
                    ParameterSpec.builder("json", JSON)
                        .defaultValue("Json { ignoreUnknownKeys = true }")
                        .build()
                )
                .build()
        )
        
        // Add properties
        builder.addProperty(
            PropertySpec.builder("httpClient", KTOR_CLIENT, KModifier.PRIVATE)
                .initializer("httpClient")
                .build()
        )
        builder.addProperty(
            PropertySpec.builder("baseUrl", String::class, KModifier.PRIVATE)
                .initializer("baseUrl")
                .build()
        )
        builder.addProperty(
            PropertySpec.builder("json", JSON, KModifier.PRIVATE)
                .initializer("json")
                .build()
        )
        
        // Generate methods for operations
        operations.forEach { operationContext ->
            builder.addFunction(generateOperationFunction(operationContext, packageName))
        }
        
        return builder.build()
    }
    
    private fun generateOperationFunction(operationContext: OperationContext, packageName: String): FunSpec {
        val operation = operationContext.operation
        val funcBuilder = FunSpec.builder(operation.operationId ?: "operation")
        
        if (config.useCoroutines) {
            funcBuilder.addModifiers(KModifier.SUSPEND)
        }
        
        // Add KDoc
        operation.summary?.let { funcBuilder.addKdoc(it) }
        
        // Add parameters
        operation.parameters?.forEach { param ->
            val paramType = typeMapper.mapType(param.schema!!, !param.required)
            funcBuilder.addParameter(
                ParameterSpec.builder(
                    param.name.sanitizeParameterName(),
                    paramType.toTypeName()
                ).apply {
                    if (!param.required) {
                        defaultValue("null")
                    }
                }.build()
            )
        }
        
        // Add request body parameter
        operation.requestBody?.let { requestBody ->
            val contentType = requestBody.content.keys.firstOrNull() ?: "application/json"
            val schema = requestBody.content[contentType]?.schema
            if (schema != null) {
                val bodyType = typeMapper.mapType(schema, !requestBody.required)
                funcBuilder.addParameter(
                    ParameterSpec.builder("request", bodyType.toTypeName())
                        .apply {
                            if (!requestBody.required) {
                                defaultValue("null")
                            }
                        }
                        .build()
                )
            }
        }
        
        // Determine return type
        val returnType = determineReturnType(operation, packageName)
        funcBuilder.returns(returnType)
        
        // Generate function body
        val codeBuilder = CodeBlock.builder()
        
        if (returnType != UNIT) {
            codeBuilder.beginControlFlow("return httpClient.request")
        } else {
            codeBuilder.beginControlFlow("httpClient.request")
        }
        
        // Set URL with path parameters
        val pathWithParams = operationContext.path.replace("\\{(.+?)\\}".toRegex()) { matchResult ->
            val paramName = matchResult.groupValues[1].sanitizeParameterName()
            "\$$paramName"
        }
        codeBuilder.addStatement("url(\"\$baseUrl%L\")", pathWithParams)
        
        // Set method
        val httpMethodName = operationContext.method.name.lowercase().replaceFirstChar { it.uppercase() }
        codeBuilder.addStatement("method = %T.%L", 
            ClassName("io.ktor.http", "HttpMethod"), 
            httpMethodName
        )
        
        // Add headers
        codeBuilder.addStatement("contentType(%T.Application.Json)",
            ClassName("io.ktor.http", "ContentType")
        )
        
        // Add parameters
        operation.parameters?.forEach { param ->
            when (param.`in`) {
                ParameterLocation.QUERY -> {
                    val paramName = param.name.sanitizeParameterName()
                    if (param.required) {
                        codeBuilder.addStatement("parameter(%S, %L)", param.name, paramName)
                    } else {
                        codeBuilder.addStatement("%L?.let { parameter(%S, it) }", paramName, param.name)
                    }
                }
                ParameterLocation.HEADER -> {
                    val paramName = param.name.sanitizeParameterName()
                    if (param.required) {
                        codeBuilder.addStatement("header(%S, %L)", param.name, paramName)
                    } else {
                        codeBuilder.addStatement("%L?.let { header(%S, it) }", paramName, param.name)
                    }
                }
                ParameterLocation.PATH -> {
                    // Path parameters are handled in URL construction
                }
                else -> {}
            }
        }
        
        // Add request body
        operation.requestBody?.let {
            codeBuilder.addStatement("setBody(request)")
        }
        
        if (returnType != UNIT) {
            codeBuilder.endControlFlow()
            codeBuilder.add(".%T<%T>()", 
                ClassName("io.ktor.client.call", "body"),
                returnType
            )
        } else {
            codeBuilder.endControlFlow()
        }
        
        funcBuilder.addCode(codeBuilder.build())
        
        return funcBuilder.build()
    }
    
    private fun determineReturnType(operation: Operation, packageName: String): TypeName {
        // Get successful response (2xx)
        val successResponse = operation.responses.entries
            .find { it.key.startsWith("2") }
            ?.value
        
        val schema = successResponse?.content?.values?.firstOrNull()?.schema
        
        return if (schema != null) {
            typeMapper.mapType(schema, false).toTypeName()
        } else {
            UNIT
        }
    }
    
    override fun generateAuthHelpers(spec: OpenApiSpec, packageName: String): List<GeneratedFile> {
        if (!config.generateAuthHelpers) {
            return emptyList()
        }
        
        val files = mutableListOf<GeneratedFile>()
        
        // Generate auth configuration classes
        spec.components?.securitySchemes?.forEach { (name, scheme) ->
            when (scheme.type) {
                SecuritySchemeType.OAUTH2 -> {
                    files.add(generateOAuth2Config(name, scheme, packageName))
                }
                else -> {
                    // Handle other auth types
                }
            }
        }
        
        // Generate auth helper
        if (files.isNotEmpty()) {
            files.add(generateAuthHelper(spec, packageName))
        }
        
        return files
    }
    
    private fun generateOAuth2Config(
        name: String,
        scheme: SecurityScheme,
        packageName: String
    ): GeneratedFile {
        val configClass = TypeSpec.classBuilder("OAuth2Config")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("authorizationUrl", String::class)
                    .addParameter("tokenUrl", String::class)
                    .addParameter("scopes", List::class.parameterizedBy(String::class))
                    .build()
            )
            .addProperty(
                PropertySpec.builder("authorizationUrl", String::class)
                    .initializer("authorizationUrl")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("tokenUrl", String::class)
                    .initializer("tokenUrl")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("scopes", List::class.parameterizedBy(String::class))
                    .initializer("scopes")
                    .build()
            )
            .build()
        
        val fileSpec = FileSpec.builder(packageName, "AuthConfig")
            .addType(configClass)
            .build()
        
        val relativePath = PackageName(packageName).toPath() + "/AuthConfig.kt"
        return GeneratedFile(relativePath, fileSpec.toString())
    }
    
    private fun generateAuthHelper(spec: OpenApiSpec, packageName: String): GeneratedFile {
        val helperClass = TypeSpec.classBuilder("AuthHelper")
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("httpClient", KTOR_CLIENT)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("httpClient", KTOR_CLIENT)
                    .initializer("httpClient")
                    .addModifiers(KModifier.PRIVATE)
                    .build()
            )
            .addFunction(
                FunSpec.builder("configureAuth")
                    .addParameter("config", OAuth2_CONFIG)
                    .addParameter("tokenManager", TOKEN_MANAGER)
                    .returns(KTOR_CLIENT)
                    .addCode("// Configure OAuth2 authentication\n")
                    .addStatement(
                        "return %T.create(\n" +
                        "    tokenEndpoint = config.tokenEndpoint,\n" +
                        "    clientId = config.clientId,\n" +
                        "    clientSecret = config.clientSecret,\n" +
                        "    redirectUri = config.redirectUri,\n" +
                        "    baseUrl = config.authorizationEndpoint,\n" +
                        "    tokenManager = tokenManager\n" +
                        ")",
                        OAuth2_CLIENT
                    )
                    .build()
            )
            .build()
        
        val fileSpec = FileSpec.builder(packageName, "AuthHelper")
            .addType(helperClass)
            .addImport("com.codingfeline.openapikotlin.runtime.auth", "OAuth2Client", "OAuth2Config", "TokenManager")
            .addImport("io.ktor.client", "HttpClient")
            .build()
        
        val relativePath = PackageName(packageName).toPath() + "/AuthHelper.kt"
        return GeneratedFile(relativePath, fileSpec.toString())
    }
    
    private fun FileSpec.Builder.addImports(): FileSpec.Builder {
        return this
            .addImport("io.ktor.client", "HttpClient")
            .addImport("io.ktor.client.request", "request", "parameter", "header", "setBody", "url")
            .addImport("io.ktor.client.call", "body")
            .addImport("io.ktor.http", "HttpMethod", "ContentType", "contentType")
            .addImport("kotlinx.serialization.json", "Json")
            .apply {
                // Models are imported explicitly when needed
            }
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
                "LocalDate" -> ClassName("kotlinx.datetime", "LocalDate")
                "Instant" -> ClassName("kotlinx.datetime", "Instant")
                else -> ClassName("kotlin", simpleName)
            }
        }
        
        return if (isNullable) baseType.copy(nullable = true) else baseType
    }
    
    private fun String.toCamelCase(): String {
        // Handle special characters by replacing them with underscores first
        val sanitized = replace(".", "_")
            .replace("[", "_")
            .replace("]", "_")
            .replace(" ", "_")
            .replace("-", "_")
            .replace("/", "_")
            .replace("\\", "_")
        
        return sanitized.split("_")
            .filter { it.isNotEmpty() }
            .mapIndexed { index, part ->
                if (index == 0) part.lowercase()
                else part.lowercase().replaceFirstChar { it.uppercase() }
            }
            .joinToString("")
    }
    
    private fun String.sanitizeParameterName(): String {
        val camelCase = this.toCamelCase()
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
            else -> camelCase
        }
    }
    
    private fun collectSchemaTypes(schema: Schema, types: MutableSet<String>) {
        // Handle direct references
        schema.`$ref`?.let { ref ->
            val typeName = ref.substringAfterLast("/")
            types.add(typeName)
            return
        }
        
        // Handle arrays
        schema.items?.let { itemSchema ->
            collectSchemaTypes(itemSchema, types)
        }
        
        // Handle object properties
        schema.properties?.values?.forEach { propSchema ->
            collectSchemaTypes(propSchema, types)
        }
        
        // Handle schema compositions
        schema.allOf?.forEach { subSchema ->
            collectSchemaTypes(subSchema, types)
        }
        schema.oneOf?.forEach { subSchema ->
            collectSchemaTypes(subSchema, types)
        }
        schema.anyOf?.forEach { subSchema ->
            collectSchemaTypes(subSchema, types)
        }
        
        // Handle additionalProperties
        if (schema.additionalProperties is Schema) {
            collectSchemaTypes(schema.additionalProperties as Schema, types)
        }
    }
}