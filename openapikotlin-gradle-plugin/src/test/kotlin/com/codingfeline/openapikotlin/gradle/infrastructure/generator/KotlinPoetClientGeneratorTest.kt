package com.codingfeline.openapikotlin.gradle.infrastructure.generator

import com.codingfeline.openapikotlin.gradle.ClientConfig
import com.codingfeline.openapikotlin.gradle.domain.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.test.*

class KotlinPoetClientGeneratorTest {
    
    private lateinit var typeMapper: KotlinPoetTypeMapper
    private lateinit var generator: KotlinPoetClientGenerator
    
    @BeforeEach
    fun setup() {
        typeMapper = KotlinPoetTypeMapper("com.example.api")
        val config = ClientConfig(
            generateClient = true,
            clientClassName = "ApiClient",
            generateErrorHandling = true,
            generateAuthHelpers = true,
            useCoroutines = true
        )
        generator = KotlinPoetClientGenerator(typeMapper, config)
    }
    
    @Test
    fun `should generate client with operations`() {
        // Given
        val spec = OpenApiSpec(
            openapi = "3.0.0",
            info = Info(
                title = "Test API",
                version = "1.0.0"
            ),
            servers = listOf(
                Server(url = "https://api.example.com/v1")
            ),
            paths = mapOf(
                "/users" to PathItem(
                    get = Operation(
                        operationId = "getUsers",
                        summary = "Get all users",
                        responses = mapOf(
                            "200" to Response(
                                description = "Success",
                                content = mapOf(
                                    "application/json" to MediaType(
                                        schema = Schema(
                                            type = SchemaType.ARRAY,
                                            items = Schema(`$ref` = "#/components/schemas/User")
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    post = Operation(
                        operationId = "createUser",
                        summary = "Create a user",
                        requestBody = RequestBody(
                            required = true,
                            content = mapOf(
                                "application/json" to MediaType(
                                    schema = Schema(`$ref` = "#/components/schemas/CreateUserRequest")
                                )
                            )
                        ),
                        responses = mapOf(
                            "201" to Response(
                                description = "Created",
                                content = mapOf(
                                    "application/json" to MediaType(
                                        schema = Schema(`$ref` = "#/components/schemas/User")
                                    )
                                )
                            )
                        )
                    )
                ),
                "/users/{id}" to PathItem(
                    get = Operation(
                        operationId = "getUser",
                        summary = "Get user by ID",
                        parameters = listOf(
                            Parameter(
                                name = "id",
                                `in` = ParameterLocation.PATH,
                                required = true,
                                schema = Schema(type = SchemaType.STRING)
                            )
                        ),
                        responses = mapOf(
                            "200" to Response(
                                description = "Success",
                                content = mapOf(
                                    "application/json" to MediaType(
                                        schema = Schema(`$ref` = "#/components/schemas/User")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        
        val operations = listOf(
            OperationContext(spec.paths["/users"]!!.get!!, "/users", HttpMethod.GET),
            OperationContext(spec.paths["/users"]!!.post!!, "/users", HttpMethod.POST),
            OperationContext(spec.paths["/users/{id}"]!!.get!!, "/users/{id}", HttpMethod.GET)
        )
        
        // When
        val result = generator.generateClient(spec, operations, "com.example.api.client")
        
        // Then
        assertEquals("com/example/api/client/ApiClient.kt", result.relativePath)
        
        // Verify imports
        assertContains(result.content, "import io.ktor.client.HttpClient")
        assertContains(result.content, "import io.ktor.client.request.request")
        assertContains(result.content, "import io.ktor.client.call.body")
        assertContains(result.content, "import kotlinx.serialization.json.Json")
        assertContains(result.content, "import com.example.api.models.User")
        assertContains(result.content, "import com.example.api.models.CreateUserRequest")
        
        // Verify class structure
        assertContains(result.content, "class ApiClient(")
        assertContains(result.content, "private val httpClient: HttpClient")
        assertContains(result.content, "private val baseUrl: String")
        assertContains(result.content, "private val json: Json")
        
        // Verify operations
        assertContains(result.content, "suspend fun getUsers()")
        assertContains(result.content, "httpClient.request")
        assertContains(result.content, "\"\$baseUrl/users\"")
        assertContains(result.content, "body<List<User>>()")
        
        assertContains(result.content, "suspend fun createUser(")
        assertContains(result.content, "request: CreateUserRequest")
        assertContains(result.content, "method = HttpMethod.POST")
        assertContains(result.content, "setBody(request)")
        assertContains(result.content, "body<User>()")
        
        assertContains(result.content, "suspend fun getUser(")
        assertContains(result.content, "id: String")
        assertContains(result.content, "\"\$baseUrl/users/\$id\"")
    }
    
    @Test
    fun `should generate auth helpers for OAuth2`() {
        // Given
        val spec = OpenApiSpec(
            openapi = "3.0.0",
            info = Info(title = "Test API", version = "1.0.0"),
            servers = emptyList(),
            paths = emptyMap(),
            components = Components(
                securitySchemes = mapOf(
                    "oauth2" to SecurityScheme(
                        type = SecuritySchemeType.OAUTH2,
                        flows = OAuthFlows(
                            authorizationCode = OAuthFlow(
                                authorizationUrl = "https://auth.example.com/authorize",
                                tokenUrl = "https://auth.example.com/token",
                                scopes = mapOf(
                                    "read" to "Read access",
                                    "write" to "Write access"
                                )
                            )
                        )
                    )
                )
            ),
            security = listOf(
                SecurityRequirement(
                    requirements = mapOf("oauth2" to listOf("read", "write"))
                )
            )
        )
        
        // When
        val result = generator.generateAuthHelpers(spec, "com.example.api.auth")
        
        // Then
        assertTrue(result.isNotEmpty())
        
        val authConfig = result.find { it.relativePath.endsWith("AuthConfig.kt") }!!
        assertContains(authConfig.content, "data class OAuth2Config(")
        assertContains(authConfig.content, "val authorizationUrl: String")
        assertContains(authConfig.content, "val tokenUrl: String")
        assertContains(authConfig.content, "val scopes: List<String>")
        
        val authHelper = result.find { it.relativePath.endsWith("AuthHelper.kt") }!!
        assertContains(authHelper.content, "class AuthHelper(")
        assertContains(authHelper.content, "fun configureAuth(")
        assertContains(authHelper.content, "OAuth2Client")
    }
    
    @Test
    fun `should handle query parameters`() {
        // Given
        val spec = OpenApiSpec(
            openapi = "3.0.0",
            info = Info(title = "Test API", version = "1.0.0"),
            servers = emptyList(),
            paths = mapOf(
                "/search" to PathItem(
                    get = Operation(
                        operationId = "search",
                        parameters = listOf(
                            Parameter(
                                name = "q",
                                `in` = ParameterLocation.QUERY,
                                required = true,
                                schema = Schema(type = SchemaType.STRING)
                            ),
                            Parameter(
                                name = "limit",
                                `in` = ParameterLocation.QUERY,
                                required = false,
                                schema = Schema(type = SchemaType.INTEGER)
                            )
                        ),
                        responses = mapOf(
                            "200" to Response(description = "Success")
                        )
                    )
                )
            )
        )
        
        val operations = listOf(
            OperationContext(spec.paths["/search"]!!.get!!, "/search", HttpMethod.GET)
        )
        
        // When
        val result = generator.generateClient(spec, operations, "com.example.api.client")
        
        // Then
        assertContains(result.content, "suspend fun search(")
        assertContains(result.content, "q: String")
        assertContains(result.content, "limit: Int? = null")
        assertContains(result.content, "parameter(\"q\", q)")
        assertContains(result.content, "limit?.let { parameter(\"limit\", it) }")
    }
    
    @Test
    fun `should handle headers`() {
        // Given
        val spec = OpenApiSpec(
            openapi = "3.0.0",
            info = Info(title = "Test API", version = "1.0.0"),
            servers = emptyList(),
            paths = mapOf(
                "/data" to PathItem(
                    get = Operation(
                        operationId = "getData",
                        parameters = listOf(
                            Parameter(
                                name = "X-Request-ID",
                                `in` = ParameterLocation.HEADER,
                                required = true,
                                schema = Schema(type = SchemaType.STRING)
                            )
                        ),
                        responses = mapOf(
                            "200" to Response(description = "Success")
                        )
                    )
                )
            )
        )
        
        val operations = listOf(
            OperationContext(spec.paths["/data"]!!.get!!, "/data", HttpMethod.GET)
        )
        
        // When
        val result = generator.generateClient(spec, operations, "com.example.api.client")
        
        // Then
        assertContains(result.content, "xRequestId: String")
        assertContains(result.content, "header(\"X-Request-ID\", xRequestId)")
    }
    
    @Test
    fun `should generate separate clients when configured`() {
        // Given
        val config = ClientConfig(
            generateClient = true,
            clientClassName = "ApiClient",
            generateErrorHandling = true,
            generateAuthHelpers = true,
            useCoroutines = true
        )
        val separateGenerator = KotlinPoetClientGenerator(typeMapper, config)
        
        val spec = OpenApiSpec(
            openapi = "3.0.0",
            info = Info(title = "Test API", version = "1.0.0"),
            servers = emptyList(),
            paths = mapOf(
                "/users" to PathItem(
                    get = Operation(
                        operationId = "getUsers",
                        tags = listOf("users"),
                        responses = mapOf("200" to Response(description = "Success"))
                    )
                ),
                "/posts" to PathItem(
                    get = Operation(
                        operationId = "getPosts",
                        tags = listOf("posts"),
                        responses = mapOf("200" to Response(description = "Success"))
                    )
                )
            )
        )
        
        val operations = listOf(
            OperationContext(spec.paths["/users"]!!.get!!, "/users", HttpMethod.GET),
            OperationContext(spec.paths["/posts"]!!.get!!, "/posts", HttpMethod.GET)
        )
        
        // When
        val result = separateGenerator.generateClient(spec, operations, "com.example.api.client")
        
        // Then
        // Should generate a main client that delegates to tag-specific clients
        assertContains(result.content, "class ApiClient(")
        assertContains(result.content, "val users: UsersApi")
        assertContains(result.content, "val posts: PostsApi")
    }
}