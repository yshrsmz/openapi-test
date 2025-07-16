package com.codingfeline.openapikotlin.gradle.infrastructure.parser

import com.codingfeline.openapikotlin.gradle.application.OpenApiParser
import com.codingfeline.openapikotlin.gradle.domain.model.*
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem as SwaggerPathItem
import io.swagger.v3.oas.models.Operation as SwaggerOperation
import io.swagger.v3.oas.models.media.Schema as SwaggerSchema
import io.swagger.v3.oas.models.parameters.Parameter as SwaggerParameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.security.SecurityScheme as SwaggerSecurityScheme
import java.io.File

/**
 * Adapter for Swagger Parser library
 */
class SwaggerParserAdapter : OpenApiParser {
    
    override fun parse(specFile: File): OpenApiSpec {
        val parseResult = OpenAPIV3Parser().read(specFile.absolutePath)
        
        if (parseResult == null) {
            throw IllegalArgumentException("Failed to parse OpenAPI specification: ${specFile.name}")
        }
        
        return mapTodomainModel(parseResult)
    }
    
    private fun mapTodomainModel(openApi: OpenAPI): OpenApiSpec {
        return OpenApiSpec(
            openapi = openApi.openapi ?: "3.0.0",
            info = mapInfo(openApi.info),
            servers = openApi.servers?.map { mapServer(it) } ?: emptyList(),
            paths = openApi.paths?.mapValues { (_, pathItem) -> 
                mapPathItem(pathItem) 
            } ?: emptyMap(),
            components = openApi.components?.let { mapComponents(it) },
            security = openApi.security?.map { mapSecurityRequirement(it) }
        )
    }
    
    private fun mapInfo(info: io.swagger.v3.oas.models.info.Info): Info {
        return Info(
            title = info.title,
            version = info.version,
            description = info.description,
            termsOfService = info.termsOfService,
            contact = info.contact?.let { Contact(it.name, it.url, it.email) },
            license = info.license?.let { License(it.name, it.url) }
        )
    }
    
    private fun mapServer(server: io.swagger.v3.oas.models.servers.Server): Server {
        return Server(
            url = server.url,
            description = server.description,
            variables = server.variables?.mapValues { (_, variable) ->
                ServerVariable(
                    enum = variable.enum,
                    default = variable.default,
                    description = variable.description
                )
            }
        )
    }
    
    private fun mapPathItem(pathItem: SwaggerPathItem): PathItem {
        return PathItem(
            summary = pathItem.summary,
            description = pathItem.description,
            get = pathItem.get?.let { mapOperation(it) },
            post = pathItem.post?.let { mapOperation(it) },
            put = pathItem.put?.let { mapOperation(it) },
            delete = pathItem.delete?.let { mapOperation(it) },
            patch = pathItem.patch?.let { mapOperation(it) },
            head = pathItem.head?.let { mapOperation(it) },
            options = pathItem.options?.let { mapOperation(it) },
            trace = pathItem.trace?.let { mapOperation(it) },
            parameters = pathItem.parameters?.map { mapParameter(it) }
        )
    }
    
    private fun mapOperation(operation: SwaggerOperation): Operation {
        return Operation(
            operationId = operation.operationId,
            summary = operation.summary,
            description = operation.description,
            tags = operation.tags,
            externalDocs = operation.externalDocs?.let {
                ExternalDocumentation(it.description, it.url)
            },
            parameters = operation.parameters?.map { mapParameter(it) },
            requestBody = operation.requestBody?.let { mapRequestBody(it) },
            responses = operation.responses?.mapValues { (_, response) ->
                mapResponse(response)
            } ?: emptyMap(),
            deprecated = operation.deprecated ?: false,
            security = operation.security?.map { mapSecurityRequirement(it) },
            servers = operation.servers?.map { mapServer(it) }
        )
    }
    
    private fun mapParameter(parameter: SwaggerParameter): Parameter {
        return Parameter(
            name = parameter.name,
            `in` = mapParameterLocation(parameter.`in`),
            description = parameter.description,
            required = parameter.required ?: false,
            deprecated = parameter.deprecated ?: false,
            allowEmptyValue = parameter.allowEmptyValue ?: false,
            schema = parameter.schema?.let { mapSchema(it) },
            example = parameter.example
        )
    }
    
    private fun mapParameterLocation(location: String): ParameterLocation {
        return when (location.lowercase()) {
            "query" -> ParameterLocation.QUERY
            "header" -> ParameterLocation.HEADER
            "path" -> ParameterLocation.PATH
            "cookie" -> ParameterLocation.COOKIE
            else -> throw IllegalArgumentException("Unknown parameter location: $location")
        }
    }
    
    private fun mapRequestBody(requestBody: io.swagger.v3.oas.models.parameters.RequestBody): RequestBody {
        return RequestBody(
            description = requestBody.description,
            content = requestBody.content?.mapValues { (_, mediaType) ->
                MediaType(
                    schema = mediaType.schema?.let { mapSchema(it) },
                    example = mediaType.example,
                    examples = mediaType.examples?.mapValues { (_, example) ->
                        Example(
                            summary = example.summary,
                            description = example.description,
                            value = example.value,
                            externalValue = example.externalValue
                        )
                    }
                )
            } ?: emptyMap(),
            required = requestBody.required ?: false
        )
    }
    
    private fun mapResponse(response: ApiResponse): Response {
        return Response(
            description = response.description ?: "",
            headers = response.headers?.mapValues { (_, header) ->
                Header(
                    description = header.description,
                    required = header.required ?: false,
                    deprecated = header.deprecated ?: false,
                    schema = header.schema?.let { mapSchema(it) },
                    example = header.example
                )
            },
            content = response.content?.mapValues { (_, mediaType) ->
                MediaType(
                    schema = mediaType.schema?.let { mapSchema(it) },
                    example = mediaType.example
                )
            }
        )
    }
    
    private fun mapSchema(schema: SwaggerSchema<*>): Schema {
        return Schema(
            type = schema.type?.let { mapSchemaType(it) },
            format = schema.format,
            title = schema.title,
            description = schema.description,
            default = schema.default,
            nullable = schema.nullable ?: false,
            readOnly = schema.readOnly ?: false,
            writeOnly = schema.writeOnly ?: false,
            deprecated = schema.deprecated ?: false,
            required = (schema.required ?: emptyList()).toList(),
            enum = schema.enum,
            minimum = schema.minimum,
            maximum = schema.maximum,
            exclusiveMinimum = schema.exclusiveMinimum ?: false,
            exclusiveMaximum = schema.exclusiveMaximum ?: false,
            minLength = schema.minLength,
            maxLength = schema.maxLength,
            pattern = schema.pattern,
            minItems = schema.minItems,
            maxItems = schema.maxItems,
            uniqueItems = schema.uniqueItems ?: false,
            properties = schema.properties?.mapValues { (_, prop) -> 
                mapSchema(prop) 
            },
            additionalProperties = schema.additionalProperties,
            items = (schema as? io.swagger.v3.oas.models.media.ArraySchema)?.items?.let { 
                mapSchema(it) 
            },
            `$ref` = schema.`$ref`,
            discriminator = schema.discriminator?.let {
                Discriminator(
                    propertyName = it.propertyName,
                    mapping = it.mapping
                )
            },
            allOf = schema.allOf?.map { mapSchema(it) },
            oneOf = schema.oneOf?.map { mapSchema(it) },
            anyOf = schema.anyOf?.map { mapSchema(it) },
            not = schema.not?.let { mapSchema(it) }
        )
    }
    
    private fun mapSchemaType(type: String): SchemaType {
        return when (type.lowercase()) {
            "string" -> SchemaType.STRING
            "number" -> SchemaType.NUMBER
            "integer" -> SchemaType.INTEGER
            "boolean" -> SchemaType.BOOLEAN
            "array" -> SchemaType.ARRAY
            "object" -> SchemaType.OBJECT
            else -> throw IllegalArgumentException("Unknown schema type: $type")
        }
    }
    
    private fun mapComponents(components: io.swagger.v3.oas.models.Components): Components {
        return Components(
            schemas = components.schemas?.mapValues { (_, schema) -> 
                mapSchema(schema) 
            },
            responses = components.responses?.mapValues { (_, response) ->
                mapResponse(response)
            },
            parameters = components.parameters?.mapValues { (_, parameter) ->
                mapParameter(parameter)
            },
            requestBodies = components.requestBodies?.mapValues { (_, requestBody) ->
                mapRequestBody(requestBody)
            },
            headers = components.headers?.mapValues { (_, header) ->
                Header(
                    description = header.description,
                    required = header.required ?: false,
                    deprecated = header.deprecated ?: false,
                    schema = header.schema?.let { mapSchema(it) }
                )
            },
            securitySchemes = components.securitySchemes?.mapValues { (_, scheme) ->
                mapSecurityScheme(scheme)
            }
        )
    }
    
    private fun mapSecurityScheme(scheme: SwaggerSecurityScheme): SecurityScheme {
        return SecurityScheme(
            type = mapSecuritySchemeType(scheme.type),
            description = scheme.description,
            name = scheme.name,
            `in` = scheme.`in`?.let { mapApiKeyLocation(it) },
            scheme = scheme.scheme,
            bearerFormat = scheme.bearerFormat,
            flows = scheme.flows?.let { mapOAuthFlows(it) },
            openIdConnectUrl = scheme.openIdConnectUrl
        )
    }
    
    private fun mapSecuritySchemeType(type: SwaggerSecurityScheme.Type): SecuritySchemeType {
        return when (type) {
            SwaggerSecurityScheme.Type.APIKEY -> SecuritySchemeType.API_KEY
            SwaggerSecurityScheme.Type.HTTP -> SecuritySchemeType.HTTP
            SwaggerSecurityScheme.Type.OAUTH2 -> SecuritySchemeType.OAUTH2
            SwaggerSecurityScheme.Type.OPENIDCONNECT -> SecuritySchemeType.OPENID_CONNECT
            else -> throw IllegalArgumentException("Unknown security scheme type: $type")
        }
    }
    
    private fun mapApiKeyLocation(location: SwaggerSecurityScheme.In): ApiKeyLocation {
        return when (location) {
            SwaggerSecurityScheme.In.QUERY -> ApiKeyLocation.QUERY
            SwaggerSecurityScheme.In.HEADER -> ApiKeyLocation.HEADER
            SwaggerSecurityScheme.In.COOKIE -> ApiKeyLocation.COOKIE
            else -> throw IllegalArgumentException("Unknown API key location: $location")
        }
    }
    
    private fun mapOAuthFlows(flows: io.swagger.v3.oas.models.security.OAuthFlows): OAuthFlows {
        return OAuthFlows(
            implicit = flows.implicit?.let { mapOAuthFlow(it) },
            password = flows.password?.let { mapOAuthFlow(it) },
            clientCredentials = flows.clientCredentials?.let { mapOAuthFlow(it) },
            authorizationCode = flows.authorizationCode?.let { mapOAuthFlow(it) }
        )
    }
    
    private fun mapOAuthFlow(flow: io.swagger.v3.oas.models.security.OAuthFlow): OAuthFlow {
        return OAuthFlow(
            authorizationUrl = flow.authorizationUrl,
            tokenUrl = flow.tokenUrl,
            refreshUrl = flow.refreshUrl,
            scopes = flow.scopes ?: emptyMap()
        )
    }
    
    private fun mapSecurityRequirement(requirement: io.swagger.v3.oas.models.security.SecurityRequirement): SecurityRequirement {
        return SecurityRequirement(
            requirements = requirement.toMap()
        )
    }
}