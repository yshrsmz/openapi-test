# Petstore API Example

This module demonstrates the OpenAPI Kotlin code generator with the classic Swagger Petstore API - a well-known example API used throughout the OpenAPI ecosystem.

## Overview

The Petstore API is a sample server that demonstrates a typical REST API for a pet store. It includes:
- Pet management (CRUD operations)
- Store orders and inventory
- User management
- OAuth2 and API key authentication

This example showcases:
- Generating code from a real-world OpenAPI 3.0 specification
- Working with various HTTP methods (GET, POST, PUT, DELETE)
- Handling different parameter types (path, query, header)
- Complex models with nested objects and arrays
- Enum types for status values
- Authentication configuration (OAuth2 and API key)

## Configuration

The plugin is configured in `build.gradle.kts`:

```kotlin
openApiKotlin {
    inputSpec = file("../openapi/petstore.json")
    outputDir = layout.buildDirectory.file("generated/openapi").get().asFile
    packageName = "com.example.petstore"
    
    models {
        generateDataAnnotations = true
        generateDefaultValues = true
    }
    
    client {
        clientClassName = "PetstoreApiClient"
        generateErrorHandling = true
        generateAuthHelpers = true
    }
}
```

## Generated Code

The plugin generates:
- **Model classes**: Pet, User, Order, Category, Tag, ApiResponse
- **PetstoreApiClient**: The main API client with all operations
- **Auth helpers**: OAuth2 configuration classes (since the spec defines OAuth2)

## Running the Example

To generate code and run the example:

```bash
# Generate the code
./gradlew :example-petstore:generateOpenApiCode

# Run the example
./gradlew :example-petstore:run
```

The example connects to the live Swagger Petstore API at https://petstore3.swagger.io and demonstrates:

1. **Finding pets by status** - Lists available pets
2. **Getting a specific pet** - Fetches details of pet with ID 1
3. **Store inventory** - Shows the count of pets by status
4. **User operations** - Creates a user and retrieves it
5. **Creating a pet** - Attempts to create a pet (fails due to authentication)

## Authentication

The Petstore API uses two authentication methods:
- **OAuth2** with implicit flow for pet operations
- **API key** for certain operations

While the generated code includes auth helpers, the example doesn't implement full OAuth2 flow for simplicity. Operations requiring authentication will fail with appropriate error messages.

## Key Features Demonstrated

1. **Complex Models**: The Pet model includes nested objects (Category), arrays (photoUrls, tags), and enums (status)
2. **Various Operations**: GET, POST, PUT, DELETE methods
3. **Parameter Types**: Path parameters (petId), query parameters (status), headers (api_key)
4. **Response Types**: Single objects, arrays, and maps (inventory)
5. **Error Handling**: Proper exception handling for 4xx errors

## Known Issues

### List Serialization Error

When running the example, you may encounter this error for endpoints returning arrays:
```
Serializer for class 'List' is not found
```

**Technical Explanation**: This occurs because of JVM type erasure. When the generated code calls `.body<List<Pet>>()`, Ktor can only see `List` at runtime, not `List<Pet>`. The kotlinx.serialization library needs the full type information (including generic parameters) to find the correct serializer, but this information is lost due to type erasure.

**Why it happens**:
1. The generated code uses Ktor's `body<T>()` function with a reified type parameter
2. For `List<Pet>`, only `List::class` is available at runtime (the `Pet` part is erased)
3. kotlinx.serialization can't find a serializer for raw `List` type
4. Single objects like `body<Pet>()` work fine because there's no generic type parameter

**Workarounds**:
1. **Manual deserialization** (modify the generated code):
   ```kotlin
   val jsonString = response.bodyAsText()
   Json.decodeFromString<List<Pet>>(jsonString)
   ```

2. **Wrapper classes** for list responses:
   ```kotlin
   @Serializable
   data class PetList(val items: List<Pet>)
   ```

3. **Future fix**: The code generator could be enhanced to use Ktor's `call.receive<T>(typeInfo)` API or implement manual deserialization for collection types.

Despite this runtime issue, the example successfully demonstrates code generation and compilation for all Petstore API endpoints.

## Next Steps

This example provides a middle ground between the simple API example and the complex Ory client. You can:
- Implement OAuth2 authentication to enable pet creation/updates
- Add more examples for order management
- Test file upload functionality (uploadImage endpoint)
- Explore the generated auth helper classes