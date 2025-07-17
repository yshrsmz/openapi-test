# Simple API Example

This module demonstrates the basic usage of the OpenAPI Kotlin code generator plugin with a minimal API specification.

## Overview

This example uses a simple OpenAPI specification with just two endpoints:
- `GET /users` - List all users
- `GET /users/{id}` - Get a specific user by ID

It's a great starting point to understand how the code generator works without the complexity of a large real-world API.

## Configuration

The plugin is configured in `build.gradle.kts`:

```kotlin
openApiKotlin {
    inputSpec = file("../openapi/simple-test.yaml")
    outputDir = layout.buildDirectory.file("generated/openapi").get().asFile
    packageName = "com.example.simple"
    
    models {
        generateDataAnnotations = true
        generateDefaultValues = true
    }
    
    client {
        clientClassName = "SimpleApiClient"
        generateErrorHandling = true
        generateAuthHelpers = false  // No auth needed for this simple example
    }
}
```

## Generated Code

The plugin generates:
1. **User.kt** - A data class representing the User model
2. **SimpleApiClient.kt** - The API client with methods for each endpoint

## Running the Example

To generate code and run the example:

```bash
# Generate the code
./gradlew :example-simple-api:generateOpenApiCode

# Run the example
./gradlew :example-simple-api:run
```

The example will attempt to make API calls to the mock URL specified in the OpenAPI spec. Since this is just a demonstration, the calls will fail with an expected error.

## What You'll Learn

This example demonstrates:
- Basic plugin configuration
- Simple model generation with required and optional fields
- Client generation with suspend functions
- How to create and configure a Ktor HTTP client
- Basic error handling

## Next Steps

After understanding this simple example, check out the `example-ory-client` module to see how the plugin handles a complex real-world API with hundreds of endpoints and models.