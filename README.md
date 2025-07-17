# OpenAPI Kotlin Code Generator

A Gradle plugin that generates Kotlin code from OpenAPI 3.0/3.1 specifications. Built with clean architecture principles, this plugin generates type-safe API clients using Ktor and kotlinx.serialization.

## Features

- 🚀 **Type-safe API Clients** - Generate strongly-typed Kotlin clients from OpenAPI specs
- 🔐 **OAuth2 Support** - Built-in OAuth2 client with PKCE support
- 🏗️ **Clean Architecture** - Hexagonal architecture with clear separation of concerns
- 📦 **Kotlin-first** - Designed specifically for Kotlin with idiomatic code generation
- 🔧 **Highly Configurable** - Extensive configuration options for generated code
- ⚡ **Ktor Integration** - Uses Ktor HTTP client for networking
- 📝 **Comprehensive Validation** - Validates models based on OpenAPI constraints
- 🔄 **Dynamic Type Support** - Handle untyped/dynamic schemas with kotlinx.serialization.json

## Project Structure

This is a multi-module Gradle project consisting of:

- **openapikotlin-runtime**: Runtime library with OAuth2 client, serializers, and base classes
- **openapikotlin-gradle-plugin**: Gradle plugin for code generation
- **example-simple-api**: Simple example demonstrating basic plugin usage
- **example-petstore**: Classic Swagger Petstore example with pets, orders, and users
- **example-ory-client**: Complex example using the Ory API (400+ models)

## Quick Start

### 1. Build and Publish Locally

```bash
# Build the entire project
./gradlew build

# Publish to local Maven repository
./gradlew publishToMavenLocal
```

### 2. Apply the Plugin

Add to your `build.gradle.kts`:

```kotlin
plugins {
    id("com.codingfeline.openapi") version "1.0.0-SNAPSHOT"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.codingfeline.openapikotlin:openapikotlin-runtime:1.0.0-SNAPSHOT")
    
    // Ktor dependencies
    implementation("io.ktor:ktor-client-core:2.3.0")
    implementation("io.ktor:ktor-client-cio:2.3.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
}
```

### 3. Configure Code Generation

```kotlin
openApiKotlin {
    // Path to your OpenAPI specification
    inputSpec = file("api/openapi.yaml")
    
    // Output directory for generated code
    outputDir = layout.buildDirectory.file("generated/openapi").get().asFile
    
    // Base package for generated code
    packageName = "com.example.api"
    
    // Model generation options
    models {
        generateDataAnnotations = true
        generateDefaultValues = true
        generateValidation = true
        useJsonElementForDynamicTypes = false  // Enable to handle untyped schemas
    }
    
    // Client generation options
    client {
        clientClassName = "ApiClient"
        generateErrorHandling = true
        generateAuthHelpers = true
    }
}
```

### 4. Generate Code

```bash
./gradlew generateOpenApiCode
```

## Dynamic Type Handling

Some OpenAPI specifications include schemas without explicit type definitions, which are meant to accept any JSON value. By default, these are mapped to Kotlin's `Any` type, which causes runtime serialization errors with kotlinx.serialization.

### The Problem

```yaml
# OpenAPI schema with no type - accepts any JSON
identityTraits:
  description: "Dynamic user traits"
```

Generated code (default):
```kotlin
public typealias identityTraits = Any  // ❌ Fails at runtime!
```

### The Solution

Enable dynamic type support to use `JsonElement` from kotlinx.serialization:

```kotlin
openApiKotlin {
    models {
        useJsonElementForDynamicTypes = true  // ✅ Enables safe dynamic types
    }
}
```

Generated code (with feature enabled):
```kotlin
public typealias identityTraits = JsonElement  // ✅ Works at runtime!
```

### Configuration Options

```kotlin
models {
    // Enable JsonElement for all untyped schemas
    useJsonElementForDynamicTypes = true
    
    // Control behavior when disabled (default: WARN)
    dynamicTypeHandling = DynamicTypeHandling.FAIL  // ALLOW, WARN, or FAIL
    
    // Override specific schemas
    schemaTypeOverrides = mapOf(
        "identityTraits" to "JsonElement",
        "metadata" to "JsonObject",
        "tags" to "JsonArray",
        "customData" to "com.example.CustomType"
    )
}
```

### Smart Type Inference

When `useJsonElementForDynamicTypes` is enabled, the plugin intelligently chooses the most appropriate type:

- **JsonObject** - For schemas with properties or additionalProperties
- **JsonArray** - For schemas with items
- **JsonElement** - For truly dynamic schemas with no hints

### Working with JsonElement

```kotlin
val response = apiClient.getIdentity(id)
val traits = response.traits  // JsonElement

// Handle different JSON types
when (traits) {
    is JsonObject -> {
        val email = traits["email"]?.jsonPrimitive?.content
        val age = traits["age"]?.jsonPrimitive?.int
    }
    is JsonArray -> {
        traits.forEach { element ->
            println(element)
        }
    }
    is JsonPrimitive -> {
        val value = traits.content
    }
}

// Convert to data class
@Serializable
data class UserTraits(val email: String, val age: Int)

val userTraits = Json.decodeFromJsonElement<UserTraits>(traits)
```

## Using the Generated Code

### Basic Usage

```kotlin
import com.example.api.client.ApiClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

suspend fun main() {
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }
    
    val apiClient = ApiClient(
        httpClient = httpClient,
        baseUrl = "https://api.example.com"
    )
    
    // Make API calls
    val users = apiClient.listUsers()
    val user = apiClient.getUser(id = "123")
}
```

### OAuth2 Authentication

```kotlin
import com.codingfeline.openapikotlin.runtime.auth.OAuth2Client
import com.codingfeline.openapikotlin.runtime.auth.OAuth2Config
import com.codingfeline.openapikotlin.runtime.auth.TokenStorage

val oauth2Config = OAuth2Config(
    clientId = "your-client-id",
    authorizationUrl = "https://auth.example.com/authorize",
    tokenUrl = "https://auth.example.com/token",
    redirectUri = "app://callback",
    scopes = listOf("read", "write")
)

val oauth2Client = OAuth2Client(
    config = oauth2Config,
    httpClient = httpClient,
    tokenStorage = InMemoryTokenStorage()
)

// Get authorization URL
val authUrl = oauth2Client.getAuthorizationUrl(state = "random-state")

// Exchange code for tokens
val tokens = oauth2Client.exchangeCodeForTokens(
    code = "auth-code",
    codeVerifier = "verifier"
)
```

## Architecture

The plugin follows hexagonal architecture principles:

```
openapikotlin-gradle-plugin/
├── domain/
│   ├── model/         # OpenAPI domain models
│   ├── service/       # Domain service interfaces
│   └── value/         # Value objects
├── application/
│   ├── usecase/       # Application use cases
│   └── dto/           # Data transfer objects
└── infrastructure/
    ├── adapter/       # External adapters (SwaggerParser)
    ├── generator/     # Code generators (KotlinPoet)
    └── gradle/        # Gradle plugin implementation
```

## Development

### Running Tests

```bash
# Run all tests
./gradlew check

# Run specific module tests
./gradlew :openapikotlin-gradle-plugin:test
```

### Example Projects

We provide three example modules demonstrating different use cases:

```bash
# Simple API example - great for getting started
# Demonstrates dynamic types with 'Config' schema
./gradlew :example-simple-api:run

# Classic Petstore example - well-known API with various features
./gradlew :example-petstore:run

# Complex real-world example using Ory API
# Shows dynamic type handling with 400+ models including 'identityTraits'
./gradlew :example-ory-client:run

# Generate code for any example
./gradlew :example-simple-api:generateOpenApiCode
./gradlew :example-petstore:generateOpenApiCode
./gradlew :example-ory-client:generateOpenApiCode
```

**Note**: The Ory client example requires `useJsonElementForDynamicTypes = true` to handle schemas like `identityTraits` that have no type definition.

## Configuration Reference

### Plugin Extension

```kotlin
openApiKotlin {
    inputSpec: File                    // Required: OpenAPI spec file
    outputDir: File                    // Required: Output directory
    packageName: String                // Required: Base package name
    
    models {
        generateDataAnnotations: Boolean   // Generate @Serializable (default: true)
        generateDefaultValues: Boolean     // Generate defaults (default: true)
        generateValidation: Boolean        // Generate validation (default: false)
        useJsonElementForDynamicTypes: Boolean // Use JsonElement for untyped schemas (default: false)
        dynamicTypeHandling: DynamicTypeHandling // ALLOW/WARN/FAIL (default: WARN)
        schemaTypeOverrides: Map<String, String> // Custom type mappings (default: empty)
    }
    
    client {
        clientClassName: String            // Client class name (default: "ApiClient")
        generateErrorHandling: Boolean     // Error handling (default: true)
        generateAuthHelpers: Boolean       // OAuth2 helpers (default: true)
        generateSeparateClients: Boolean   // Per-tag clients (default: false)
    }
    
    validation {
        failOnWarnings: Boolean           // Fail on warnings (default: false)
    }
}
```

## Supported Features

### OpenAPI Features
- ✅ OpenAPI 3.0 and 3.1
- ✅ JSON and YAML specifications
- ✅ All HTTP methods (GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS)
- ✅ Path, query, and header parameters
- ✅ Request and response bodies
- ✅ Arrays and nested objects
- ✅ Enums with proper Kotlin enum generation
- ✅ Nullable and required fields
- ✅ Default values
- ✅ Date/DateTime with kotlinx-datetime
- ✅ OAuth2 security schemes
- ✅ Reserved keyword handling
- ⚠️ Basic oneOf/anyOf support
- ❌ Callbacks
- ❌ Links
- ❌ Webhooks

### Code Generation Features
- ✅ Data classes with kotlinx.serialization
- ✅ Immutable models with null safety
- ✅ Coroutine-based API clients
- ✅ Ktor HTTP client integration
- ✅ Automatic parameter serialization
- ✅ Response deserialization
- ✅ Error handling
- ✅ OAuth2 with PKCE support

## Troubleshooting

### Common Issues

1. **"Serializer has not been found for type 'Any'"**: Enable `useJsonElementForDynamicTypes` for schemas without type definitions
2. **Unresolved types**: Ensure all referenced schemas are defined in the OpenAPI spec
3. **Reserved keywords**: The plugin automatically escapes Kotlin reserved words
4. **Special characters**: Property names with special characters are sanitized
5. **Dynamic schemas**: Use `schemaTypeOverrides` for fine-grained control over specific schemas

### Gradle Tasks

```bash
# Clean generated code
./gradlew clean

# Generate code
./gradlew generateOpenApiCode

# Build without tests
./gradlew build -x test

# Publish to local Maven
./gradlew publishToMavenLocal
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.