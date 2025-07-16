# OpenAPI Kotlin Code Generator Plugin - Design Document

## 1. Executive Summary

This document outlines the design and implementation specifications for a Gradle plugin that generates Kotlin code from OpenAPI specifications. The implementation will use the existing `openapikotlin-gradle-plugin` module in this repository. The plugin will parse OpenAPI 3.0/3.1 spec files and generate:
- Kotlin data classes with kotlinx.serialization support
- Ktor HTTP client implementations

**Target Audience**: AI Coding Agents and developers implementing this plugin.

## 2. Project Goals

### Primary Goals
1. Parse OpenAPI specifications (YAML/JSON) using Swagger Parser
2. Generate idiomatic Kotlin data classes for all schemas
3. Generate Ktor client code with suspend functions for all API operations
4. Integrate seamlessly with Gradle build process

### Non-Goals
1. Supporting other HTTP clients (Retrofit, OkHttp) - future enhancement
2. Supporting other serialization libraries (Gson, Moshi) - future enhancement
3. Generating server-side code
4. OpenAPI 2.0 (Swagger) support

## 3. Technical Requirements

### Dependencies

#### Runtime Module
```kotlin
// openapikotlin-runtime/build.gradle.kts
dependencies {
    api("io.ktor:ktor-client-core:3.2.2")
    api("io.ktor:ktor-client-auth:3.2.2")
    api("io.ktor:ktor-client-content-negotiation:3.2.2")
    api("io.ktor:ktor-serialization-kotlinx-json:3.2.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}
```

#### Plugin Module
```kotlin
// openapikotlin-gradle-plugin/build.gradle.kts
dependencies {
    implementation("io.swagger.parser.v3:swagger-parser:2.1.31")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
    implementation("com.squareup:kotlinpoet:2.2.0")
    // Runtime module is not a dependency - generated code depends on it
}
```

### Compatibility
- Gradle 8.0+
- Kotlin 2.0+
- Java 11+

## 4. Design Philosophy

### 4.1 Test-Driven Development (t_wada's Approach)

Following Takuto Wada's TDD principles:

1. **Test First, Code Later**: Write tests before implementation
2. **Red-Green-Refactor Cycle**:
   - Red: Write a failing test
   - Green: Write minimal code to make the test pass
   - Refactor: Improve code quality while keeping tests green
3. **Triangulation**: Use multiple test cases to drive general solutions
4. **Test as Documentation**: Tests serve as living documentation
5. **Small Steps**: Make progress in tiny, verifiable increments

### 4.2 Design Principles (Martin Fowler's Approach)

Following Martin Fowler's design principles:

1. **Domain-Driven Design**: Separate domain logic from infrastructure
2. **Hexagonal Architecture**: Core domain independent of external concerns
3. **Dependency Inversion**: Depend on abstractions, not concrete implementations
4. **Single Responsibility**: Each class has one reason to change
5. **Tell, Don't Ask**: Objects should tell each other what to do, not query state
6. **Immutability**: Prefer immutable objects for thread safety and clarity

## 5. Architecture

### 5.1 Module Structure

The project consists of two modules:

1. **openapikotlin-runtime**: Runtime library with shared helpers and base implementations
2. **openapikotlin-gradle-plugin**: Gradle plugin for code generation

### 5.2 Runtime Module Structure

The `openapikotlin-runtime` module provides reusable components:

```
openapikotlin-runtime/
└── src/main/kotlin/com/codingfeline/openapikotlin/runtime/
    ├── auth/
    │   ├── OAuth2Client.kt                 # OAuth2 client factory with PKCE support
    │   ├── TokenManager.kt                 # Token storage and refresh logic
    │   └── AuthInterceptor.kt             # Ktor auth interceptor
    ├── serialization/
    │   ├── DateTimeSerializers.kt         # Custom serializers for date/time
    │   └── EnumSerializer.kt              # Flexible enum serialization
    ├── http/
    │   ├── ApiClient.kt                   # Base HTTP client configuration
    │   ├── ApiException.kt                # Common exception types
    │   └── ResponseValidator.kt           # Response validation utilities
    └── validation/
        ├── ConstraintValidator.kt         # Runtime validation for constraints
        └── ValidationException.kt         # Validation error types
```

### 5.3 Plugin Module Structure

Following Martin Fowler's principles, the plugin implementation separates domain logic from infrastructure:

```
openapikotlin-gradle-plugin/
└── src/main/kotlin/com/codingfeline/openapikotlin/gradle/
    ├── OpenApiKotlinPlugin.kt              # Gradle plugin entry point (Infrastructure)
    ├── OpenApiKotlinExtension.kt           # Plugin DSL (Infrastructure)
    │
    ├── domain/                             # Core domain logic (no external dependencies)
    │   ├── model/
    │   │   ├── OpenApiSpec.kt              # Domain model for OpenAPI specification
    │   │   ├── Schema.kt                   # Domain model for schemas
    │   │   ├── Operation.kt                # Domain model for operations
    │   │   └── SecurityScheme.kt          # Domain model for security
    │   ├── service/
    │   │   ├── CodeGenerationService.kt    # Interface for code generation
    │   │   ├── TypeMappingService.kt       # Interface for type mapping
    │   │   └── ValidationService.kt        # Interface for validation
    │   └── value/
    │       ├── KotlinType.kt               # Value object for Kotlin types
    │       └── PackageName.kt              # Value object for package names
    │
    ├── application/                        # Application services (orchestration)
    │   ├── GenerateCodeUseCase.kt          # Main use case for code generation
    │   └── ValidateSpecUseCase.kt          # Use case for spec validation
    │
    ├── infrastructure/                     # External concerns
    │   ├── gradle/
    │   │   └── GenerateTask.kt             # Gradle task implementation
    │   ├── parser/
    │   │   ├── SwaggerParserAdapter.kt     # Adapter for Swagger Parser
    │   │   └── OpenApiSpecMapper.kt       # Maps external model to domain
    │   ├── generator/
    │   │   ├── KotlinPoetModelGenerator.kt # Implementation using KotlinPoet
    │   │   ├── KotlinPoetClientGenerator.kt # Implementation using KotlinPoet
    │   │   └── KotlinPoetTypeMapper.kt    # Type mapping implementation
    │   └── io/
    │       └── FileWriter.kt               # File system operations
    │
    └── test/                               # Test utilities
        ├── fixture/                        # Test fixtures
        └── builder/                        # Test data builders
```

### 5.4 Component Responsibilities

#### Runtime Module Components
- **OAuth2Client**: Provides OAuth2 Authorization Code Flow with PKCE implementation
- **TokenManager**: Handles token storage, refresh, and lifecycle management
- **ApiClient**: Base HTTP client with common configuration (timeouts, retries, logging)
- **ApiException**: Standardized error handling for API responses
- **DateTimeSerializers**: kotlinx.datetime serializers for consistent date handling
- **ConstraintValidator**: Runtime validation for OpenAPI constraints (min/max, pattern, etc.)

#### Plugin Module - Domain Layer
- **Domain Models**: Pure data structures representing OpenAPI concepts
- **Domain Services**: Interfaces defining core operations without implementation details
- **Value Objects**: Immutable objects representing domain concepts (KotlinType, PackageName)
- **No external dependencies**: Can be tested in isolation

#### Plugin Module - Application Layer
- **GenerateCodeUseCase**: Orchestrates the code generation process
- **ValidateSpecUseCase**: Coordinates specification validation
- **Transaction boundaries**: Defines units of work
- **Domain service coordination**: Combines multiple domain services

#### Plugin Module - Infrastructure Layer
- **SwaggerParserAdapter**: Adapts Swagger Parser to domain interfaces
- **KotlinPoetGenerators**: Implement code generation using KotlinPoet
- **FileWriter**: Handles file system operations
- **GenerateTask**: Gradle plugin integration

#### Key Design Patterns Applied
1. **Hexagonal Architecture**: Domain isolated from external concerns
2. **Dependency Inversion**: Infrastructure depends on domain, not vice versa
3. **Adapter Pattern**: External libraries wrapped in adapters
4. **Repository Pattern**: Abstract data access (if needed for caching)
5. **Factory Pattern**: For creating complex domain objects

## 6. Implementation Details

### 6.1 Plugin Configuration DSL

```kotlin
openApi {
    // Required configuration
    inputSpec = file("api/openapi.yaml")
    
    // Optional configuration with defaults
    outputDir = file("$buildDir/generated/openapi")
    packageName = "com.example.api"
    
    // Model generation options
    models {
        generateDataAnnotations = true
        generateDefaultValues = true
    }
    
    // Client generation options  
    client {
        generateClient = true
        clientClassName = "ApiClient"
        generateErrorHandling = true
        generateAuthHelpers = true // Generate OAuth2/Bearer auth helpers
    }
    
    // Advanced options
    validation {
        failOnWarnings = false
        strict = true
    }
}
```

### 6.2 Type Mapping Rules

| OpenAPI Type | Format | Kotlin Type |
|--------------|--------|-------------|
| integer | int32 | Int |
| integer | int64 | Long |
| number | float | Float |
| number | double | Double |
| string | - | String |
| string | date | kotlinx.datetime.LocalDate |
| string | date-time | kotlinx.datetime.Instant |
| string | uuid | String |
| boolean | - | Boolean |
| array | - | List<T> |
| object | - | Map<String, Any> or generated class |

**Note**: Date and date-time formats are exclusively mapped to kotlinx.datetime types.

### 6.3 Code Generation with KotlinPoet

The plugin uses **KotlinPoet** (Square's Kotlin code generation library) for generating all Kotlin source files. KotlinPoet provides:

- Type-safe code generation
- Proper import management
- Kotlin idiom support (data classes, default parameters, etc.)
- Clean, formatted output

#### Example KotlinPoet Usage for Model Generation:

```kotlin
package com.codingfeline.openapikotlin.gradle.generator

fun generateDataClass(schema: Schema<*>, className: String, packageName: String): FileSpec {
    val classBuilder = TypeSpec.classBuilder(className)
        .addModifiers(KModifier.DATA)
        .addAnnotation(Serializable::class)
    
    val constructor = FunSpec.constructorBuilder()
    
    schema.properties?.forEach { (propName, propSchema) ->
        val propertyName = propName.toCamelCase()
        val propertyType = mapOpenApiTypeToKotlin(propSchema)
        val isRequired = schema.required?.contains(propName) ?: false
        
        val property = PropertySpec.builder(propertyName, propertyType)
            .initializer(propertyName)
        
        if (!isRequired) {
            property.defaultValue(getDefaultValue(propertyType))
        }
        
        constructor.addParameter(
            ParameterSpec.builder(propertyName, propertyType)
                .apply { if (!isRequired) defaultValue(getDefaultValue(propertyType)) }
                .build()
        )
        
        classBuilder.addProperty(property.build())
    }
    
    classBuilder.primaryConstructor(constructor.build())
    
    return FileSpec.builder(packageName, className)
        .addType(classBuilder.build())
        .build()
}
```

### 6.4 Code Generation Examples

#### Input Schema (Petstore API)
```yaml
components:
  schemas:
    Pet:
      type: object
      required:
        - name
        - photoUrls
      properties:
        id:
          type: integer
          format: int64
          example: 10
        category:
          $ref: '#/components/schemas/Category'
        name:
          type: string
          example: doggie
        photoUrls:
          type: array
          items:
            type: string
        tags:
          type: array
          items:
            $ref: '#/components/schemas/Tag'
        status:
          type: string
          description: pet status in the store
          enum:
            - available
            - pending
            - sold
    Category:
      type: object
      properties:
        id:
          type: integer
          format: int64
          example: 1
        name:
          type: string
          example: Dogs
    Tag:
      type: object
      properties:
        id:
          type: integer
          format: int64
        name:
          type: string
```

#### Generated Data Classes
```kotlin
package com.example.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Pet(
    val name: String,
    val photoUrls: List<String>,
    val id: Long? = null,
    val category: Category? = null,
    val tags: List<Tag> = emptyList(),
    val status: PetStatus? = null
)

@Serializable
data class Category(
    val id: Long? = null,
    val name: String? = null
)

@Serializable
data class Tag(
    val id: Long? = null,
    val name: String? = null
)

@Serializable
enum class PetStatus {
    @SerialName("available")
    AVAILABLE,
    @SerialName("pending")
    PENDING,
    @SerialName("sold")
    SOLD
}
```

#### Generated Client Methods
```kotlin
package com.example.api.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import com.example.api.models.*

class PetStoreClient(private val httpClient: HttpClient) {
    
    suspend fun addPet(pet: Pet): Pet {
        return httpClient.post {
            url {
                path("pet")
            }
            contentType(ContentType.Application.Json)
            setBody(pet)
        }.body()
    }
    
    suspend fun getPetById(petId: Long): Pet {
        return httpClient.get {
            url {
                path("pet", petId.toString())
            }
        }.body()
    }
    
    suspend fun updatePet(pet: Pet): Pet {
        return httpClient.put {
            url {
                path("pet")
            }
            contentType(ContentType.Application.Json)
            setBody(pet)
        }.body()
    }
    
    suspend fun findPetsByStatus(status: PetStatus): List<Pet> {
        return httpClient.get {
            url {
                path("pet", "findByStatus")
                parameters.append("status", status.name.lowercase())
            }
        }.body()
    }
    
    suspend fun deletePet(petId: Long, apiKey: String? = null) {
        httpClient.delete {
            url {
                path("pet", petId.toString())
            }
            apiKey?.let { headers.append("api_key", it) }
        }
    }
}
```

#### OAuth2 Authorization Code Flow with PKCE Support

The generated client leverages the runtime module for OAuth2 support:

```kotlin
package com.example.api.client

import com.codingfeline.openapikotlin.runtime.auth.OAuth2Client
import com.codingfeline.openapikotlin.runtime.auth.TokenManager
import com.codingfeline.openapikotlin.runtime.http.ApiClient
import com.codingfeline.openapikotlin.runtime.http.ApiException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Generated Ory Identity API client using runtime module
 */
class OryIdentityClient(
    baseUrl: String,
    tokenEndpoint: String,
    clientId: String,
    redirectUri: String,
    tokenManager: TokenManager = TokenManager.InMemory()
) {
    // Uses OAuth2Client from runtime module
    private val httpClient = OAuth2Client.create(
        tokenEndpoint = tokenEndpoint,
        clientId = clientId,
        redirectUri = redirectUri,
        tokenManager = tokenManager,
        baseUrl = baseUrl
    )
    
    suspend fun getCurrentIdentity(): Identity {
        return try {
            httpClient.get {
                url {
                    path("sessions", "whoami")
                }
            }.body()
        } catch (e: Exception) {
            throw ApiException.from(e)
        }
    }
    
    suspend fun listIdentities(
        page: Int? = null,
        perPage: Int? = null
    ): List<Identity> {
        return try {
            httpClient.get {
                url {
                    path("admin", "identities")
                    page?.let { parameters.append("page", it.toString()) }
                    perPage?.let { parameters.append("per_page", it.toString()) }
                }
            }.body()
        } catch (e: Exception) {
            throw ApiException.from(e)
        }
    }
}

/**
 * Extension function to create client with custom configuration
 */
fun OryIdentityClient.Companion.create(
    config: OAuth2Config
): OryIdentityClient {
    return OryIdentityClient(
        baseUrl = config.baseUrl,
        tokenEndpoint = config.tokenEndpoint,
        clientId = config.clientId,
        redirectUri = config.redirectUri,
        tokenManager = config.tokenManager ?: TokenManager.InMemory()
    )
}
```

## 7. TDD Implementation Steps

Following t_wada's TDD approach with Red-Green-Refactor cycle:

### Step 1: Domain Model Tests First
#### Red Phase
1. Write test for `OpenApiSpec` domain model
2. Write test for `Schema` domain model  
3. Write test for `Operation` domain model
4. Write test for value objects (`KotlinType`, `PackageName`)

#### Green Phase
1. Implement minimal domain models to pass tests
2. Focus on data structure, not behavior yet

#### Refactor Phase
1. Extract common patterns
2. Ensure immutability

### Step 2: Domain Service Tests
#### Red Phase
1. Write test for `TypeMappingService` interface
2. Write test for `CodeGenerationService` interface
3. Write test for `ValidationService` interface

#### Green Phase
1. Create interfaces only (no implementation)
2. Define contracts through tests

### Step 3: Use Case Tests
#### Red Phase
1. Write test for `GenerateCodeUseCase`
2. Test orchestration logic with mocked services
3. Test error scenarios

#### Green Phase
1. Implement use case with dependency injection
2. Use constructor injection for testability

#### Refactor Phase
1. Extract common orchestration patterns
2. Apply Tell, Don't Ask principle

### Step 4: Infrastructure Adapter Tests
#### Red Phase
1. Write test for `SwaggerParserAdapter`
2. Test mapping from external model to domain
3. Test error handling

#### Green Phase
1. Implement adapter with minimal logic
2. Focus on translation, not business logic

#### Refactor Phase
1. Simplify mapping code
2. Handle edge cases discovered through tests

### Step 5: Code Generator Tests
#### Red Phase
1. Write test for generating simple data class
2. Add test for nullable properties
3. Add test for collections
4. Add test for enums
5. Add test for nested objects

#### Green Phase
1. Implement with KotlinPoet
2. Start with simplest case
3. Use triangulation to generalize

#### Refactor Phase
1. Extract code generation patterns
2. Create builder abstractions

### Step 6: Client Generator Tests
#### Red Phase
1. Write test for simple GET endpoint
2. Add test for POST with body
3. Add test for query parameters
4. Add test for OAuth2 authentication

#### Green Phase
1. Generate minimal Ktor client code
2. Add features incrementally

#### Refactor Phase
1. Extract HTTP method patterns
2. Generalize parameter handling

### Step 7: Integration Tests
#### Red Phase
1. Write end-to-end test with Petstore spec
2. Test generated code compilation
3. Test generated code functionality

#### Green Phase
1. Wire all components together
2. Implement Gradle task

#### Refactor Phase
1. Optimize performance
2. Improve error messages

### Step 8: Plugin DSL Tests
#### Red Phase
1. Write test for configuration parsing
2. Test validation of required fields
3. Test default values

#### Green Phase
1. Implement Gradle extension
2. Add configuration validation

### Testing Best Practices
1. **Test Naming**: Use descriptive names explaining the scenario
2. **Arrange-Act-Assert**: Clear test structure
3. **One Assertion Per Test**: Keep tests focused
4. **Test Builders**: Use builders for complex test data
5. **Parameterized Tests**: For testing multiple scenarios
6. **Property-Based Testing**: For type mapping edge cases

## 8. Error Handling

### Parser Errors
- Invalid OpenAPI spec format
- Missing required fields
- Circular references

### Generation Errors
- File write permissions
- Invalid package names
- Unsupported OpenAPI features

### Runtime Errors (in generated code)
- Network failures
- Serialization errors
- HTTP error responses

## 9. Testing Strategy

### Unit Tests
- Type mapping correctness
- Code generation output
- DSL configuration parsing

### Integration Tests
- Full OpenAPI spec parsing
- End-to-end code generation
- Gradle task execution

### Generated Code Tests
- Compilation verification
- Serialization/deserialization
- HTTP client functionality

### Recommended Test Specifications
1. **Petstore API** - The standard OpenAPI example (https://petstore3.swagger.io/)
2. **Stripe API** - Complex real-world API (https://github.com/stripe/openapi)
3. **GitHub API** - Well-documented public API
4. **@openapi/ory-client-1.20.22.json** - Ory Identity Server API for auth flows

## 10. Deliverables

### Runtime Module Files (in `openapikotlin-runtime/src/main/kotlin/com/codingfeline/openapikotlin/runtime/`)

#### Authentication
1. `auth/OAuth2Client.kt` - OAuth2 client factory with PKCE support
2. `auth/TokenManager.kt` - Token storage interface and implementations
3. `auth/AuthInterceptor.kt` - Ktor authentication interceptor

#### HTTP Client
4. `http/ApiClient.kt` - Base HTTP client configuration
5. `http/ApiException.kt` - Standardized API exceptions
6. `http/ResponseValidator.kt` - Response validation utilities

#### Serialization
7. `serialization/DateTimeSerializers.kt` - kotlinx.datetime serializers
8. `serialization/EnumSerializer.kt` - Flexible enum handling

#### Validation
9. `validation/ConstraintValidator.kt` - Runtime constraint validation
10. `validation/ValidationException.kt` - Validation error types

### Plugin Module Files (in `openapikotlin-gradle-plugin/src/main/kotlin/com/codingfeline/openapikotlin/gradle/`)

#### Domain Layer
1. `domain/model/OpenApiSpec.kt` - Domain model for specifications
2. `domain/model/Schema.kt` - Domain model for schemas
3. `domain/model/Operation.kt` - Domain model for operations
4. `domain/service/CodeGenerationService.kt` - Code generation interface
5. `domain/service/TypeMappingService.kt` - Type mapping interface
6. `domain/value/KotlinType.kt` - Kotlin type value object

#### Application Layer
7. `application/GenerateCodeUseCase.kt` - Main generation orchestration
8. `application/ValidateSpecUseCase.kt` - Validation orchestration

#### Infrastructure Layer
9. `infrastructure/gradle/GenerateTask.kt` - Gradle task
10. `infrastructure/parser/SwaggerParserAdapter.kt` - Parser adapter
11. `infrastructure/generator/KotlinPoetModelGenerator.kt` - Model generation
12. `infrastructure/generator/KotlinPoetClientGenerator.kt` - Client generation
13. `OpenApiKotlinPlugin.kt` - Plugin entry point
14. `OpenApiKotlinExtension.kt` - DSL configuration

### Supporting Files
1. `openapikotlin-runtime/build.gradle.kts` - Runtime module build config
2. `openapikotlin-gradle-plugin/build.gradle.kts` - Plugin module build config
3. `openapikotlin-gradle-plugin/src/main/resources/META-INF/gradle-plugins/com.codingfeline.openapi.properties`
4. Tests for both modules
5. `README.md` - Usage documentation
6. `settings.gradle.kts` - Updated to include both modules

### Generated Output
1. Kotlin data classes with `@Serializable`
2. Ktor client class with suspend functions
3. Proper package structure
4. Compilation-ready code

## 11. Success Criteria

1. Plugin successfully parses valid OpenAPI 3.0/3.1 specs
2. Generated data classes are immutable (all properties use `val`)
3. Generated data classes compile without errors
4. Generated client code compiles and functions correctly
5. All required fields are non-nullable in data classes
6. Optional fields have appropriate defaults
7. Collections are properly typed
8. Enums are generated as sealed classes or enum classes
9. Client methods handle all parameter types correctly
10. Integration with Gradle build is seamless
11. Generated code follows Kotlin conventions
12. Runtime module provides reusable OAuth2/PKCE implementation
13. Generated code has minimal boilerplate due to runtime module
14. Runtime module can be used independently of code generation

## 12. Future Enhancements

1. Support for additional HTTP clients (Retrofit, OkHttp)
2. Alternative serialization libraries (Gson, Moshi)
3. Server stub generation
4. OpenAPI 2.0 support
5. Custom template support
6. Incremental generation
7. Multi-module support
8. Authentication handling
9. Custom type mappings
10. Response validation

## 13. References

- [OpenAPI Specification](https://spec.openapis.org/oas/latest.html)
- [Swagger Parser Documentation](https://github.com/swagger-api/swagger-parser)
- [Ktor Client Documentation](https://ktor.io/docs/client.html)
- [kotlinx.serialization Guide](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md)
- [Gradle Plugin Development](https://docs.gradle.org/current/userguide/custom_plugins.html)
- [KotlinPoet Library](https://square.github.io/kotlinpoet/)

---

This design document provides a complete specification for implementing the OpenAPI Kotlin code generator plugin. Follow the implementation steps sequentially, referring to the examples and specifications provided throughout the document.