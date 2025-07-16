# OpenAPI Kotlin Example Module

This module demonstrates how to use the OpenAPI Kotlin code generator plugin.

## Configuration

The plugin is configured in `build.gradle.kts`:

```kotlin
openApi {
    inputSpec = file("../openapi/ory-client-1.20.22.json")
    outputDir = file("$buildDir/generated/openapi")
    packageName = "com.example.api.ory"
    
    models {
        generateDataAnnotations = true
        generateDefaultValues = true
    }
    
    client {
        generateClient = true
        clientClassName = "OryApiClient"
        generateErrorHandling = true
        generateAuthHelpers = true
    }
}
```

## Generated Code

When the plugin runs, it will generate:

1. **Data Classes** - Kotlin data classes for all OpenAPI schemas
   - With `@Serializable` annotations
   - Proper null safety based on required fields
   - Default values for optional fields
   - kotlinx.datetime types for date/time fields

2. **API Client** - Type-safe Ktor HTTP client
   - Suspend functions for all API operations
   - OAuth2 authentication support
   - Automatic error handling
   - Request/response validation

## Usage Example

```kotlin
// Create OAuth2 configuration
val oauth2Config = OAuth2Config(
    authorizationEndpoint = "https://auth.example.com/oauth2/auth",
    tokenEndpoint = "https://auth.example.com/oauth2/token",
    clientId = "your-client-id",
    redirectUri = "app://callback"
)

// Create API client
val client = OryApiClient.create(
    baseUrl = "https://api.example.com",
    oauth2Config = oauth2Config,
    tokenManager = TokenManager.InMemory()
)

// Make API calls
val identity = client.getCurrentIdentity()
println("Current user: ${identity.email}")
```

## Running the Example

To generate code and run the example:

```bash
./gradlew :example:generateOpenApiCode
./gradlew :example:run
```

## Testing

The generated code can be tested using Ktor's MockEngine:

```kotlin
val mockEngine = MockEngine { request ->
    // Mock your API responses
}

val client = OryApiClient.create(
    baseUrl = "https://api.example.com",
    engine = mockEngine
)
```