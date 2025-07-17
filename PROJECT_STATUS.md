# OpenAPI Kotlin Code Generator - Project Status

## Overview
This project is a Gradle plugin that generates Kotlin code from OpenAPI specifications. It follows clean architecture principles with domain-driven design and uses KotlinPoet for code generation.

## Current Implementation Status

### ✅ Completed Features

#### 1. Core Plugin Infrastructure
- **Gradle Plugin Setup**: Full Gradle plugin infrastructure with proper task configuration
- **Clean Architecture**: Separated into domain, application, and infrastructure layers
- **Hexagonal Architecture**: Ports and adapters pattern for extensibility

#### 2. Model Generation
- **Data Classes**: Generates Kotlin data classes with proper nullability
- **Enums**: Generates enums for string types with enum values
- **Nested Enums**: Automatically generates enum types for properties with inline enum values
- **Default Values**: Optional properties have default null values
- **kotlinx.serialization**: All models use `@Serializable` annotation
- **SerialName**: Properly handles property name mapping
- **Property Name Sanitization**: Handles reserved keywords and special characters

#### 3. Client Generation
- **Ktor Client**: Generates HTTP clients using Ktor
- **All HTTP Methods**: Supports GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS, TRACE
- **Parameter Handling**:
  - Path parameters with proper substitution
  - Query parameters with null filtering
  - Header parameters with proper name conversion
  - Request body support
- **Response Handling**: Type-safe responses with proper error handling
- **Authentication**: OAuth2 helper generation with PKCE support

#### 4. Schema Composition Support
- **allOf**: 
  - Merges properties from multiple schemas
  - Recursive resolution for nested compositions
  - Handles schema references properly
- **oneOf**:
  - Generates sealed interfaces
  - Discriminator support
  - Automatic interface implementation
- **anyOf**:
  - Generates wrapper classes
  - Basic implementation with Any type

#### 5. Special Features
- **Special Types Generation**: Handles undefined types like NullUUID, Time, etc.
- **Improved CamelCase**: Preserves existing camelCase in property names
- **Import Management**: Smart import handling without wildcards
- **Type Mapping**: Comprehensive type mapping including collections and maps

### 🚧 Pending Features

#### 1. Separate Clients by Tags (Medium Priority)
- Generate multiple client classes based on OpenAPI tags
- Useful for organizing large APIs
- Test already written but currently skipped

#### 2. Future Enhancements (Not Started)
- **Validation**: Pattern matching, min/max constraints
- **Advanced anyOf/oneOf**: Custom serializers for better type safety
- **Multipart Support**: File upload handling
- **Retry Logic**: Configurable retry mechanisms
- **Custom Interceptors**: Plugin system for client customization
- **WebSocket Support**: Real-time communication
- **Better Error Messages**: More helpful error reporting
- **Spec Validation Task**: Separate Gradle task for validation

## Project Structure

```
openapi-test/
├── buildSrc/                    # Convention plugins
├── openapikotlin-gradle-plugin/ # Main plugin module
│   ├── domain/                  # Domain models and interfaces
│   ├── application/             # Use cases
│   └── infrastructure/          # Implementations
├── example-ory-client/          # Example project using Ory API
└── openapi/                     # Test OpenAPI specifications
```

## Key Classes

### Domain Layer
- `OpenApiSpec`: Domain model for OpenAPI specification
- `Schema`: Domain model for OpenAPI schemas
- `Operation`: Domain model for API operations
- `CodeGenerationService`: Interface for code generators

### Application Layer
- `GenerateCodeUseCase`: Main use case orchestrating code generation
- `OpenApiParser`: Port for parsing OpenAPI specs

### Infrastructure Layer
- `KotlinPoetModelGenerator`: Generates model classes
- `KotlinPoetClientGenerator`: Generates client classes
- `SwaggerParserAdapter`: Parses OpenAPI specs using Swagger Parser
- `SpecialTypesGenerator`: Handles undefined types

## Testing

### Test Coverage
- **42 tests total**: 41 passing, 1 skipped
- **Unit Tests**: Core functionality and generators
- **Integration Tests**: Full code generation pipeline
- **Real-world Tests**: Tested with Petstore and Ory APIs

### Key Test Files
- `SchemaCompositionTest`: Tests for allOf/oneOf/anyOf
- `KotlinPoetClientGeneratorTest`: Client generation tests
- `RealWorldSpecsIntegrationTest`: Tests with real API specs

## Usage Example

```kotlin
// In build.gradle.kts
plugins {
    id("com.codingfeline.openapikotlin") version "1.0.0"
}

openApiKotlin {
    inputSpec = file("api-spec.yaml")
    outputDir = file("$buildDir/generated")
    packageName = "com.example.api"
    
    models {
        generateDataAnnotations = true
        generateDefaultValues = true
        useKotlinxDatetime = true
    }
    
    client {
        clientClassName = "ApiClient"
        generateErrorHandling = true
        generateAuthHelpers = true
    }
}
```

## Recent Changes

### Latest Commits
1. **Schema Composition Support**: Added comprehensive allOf/oneOf/anyOf support
2. **Special Types**: Added generation for undefined types in specs
3. **Enum Generation**: Fixed enum generation for nested properties
4. **CamelCase Fix**: Improved property name handling

## Known Issues
- **List/Array Response Deserialization**: Generated code using `.body<List<T>>()` fails at runtime due to JVM type erasure. Ktor + kotlinx.serialization cannot resolve serializers for generic collection types. Workaround: Use manual deserialization with `Json.decodeFromString<List<T>>()`.
- anyOf implementation is basic and could use custom serializers
- No support for XML content types
- No support for webhooks/callbacks
- Limited OpenAPI 3.1 support

## Next Steps

### High Priority
1. Implement separate clients by tags feature
2. Add comprehensive validation support
3. Improve anyOf/oneOf with custom serializers

### Medium Priority
1. Add multipart/form-data support
2. Implement retry logic and circuit breakers
3. Add WebSocket support

### Low Priority
1. Add XML support
2. Improve error messages
3. Add more configuration options

## Development Guidelines

### Adding New Features
1. Start with tests (TDD approach)
2. Update domain models if needed
3. Implement in appropriate layer
4. Ensure backward compatibility
5. Update documentation

### Code Style
- Use Kotlin idioms
- Follow existing patterns
- No comments unless necessary
- Comprehensive test coverage

## Dependencies
- Kotlin 2.1.21
- KotlinPoet 2.2.0
- Swagger Parser 2.1.31
- Ktor (generated code dependency)
- kotlinx.serialization 1.9.0
- kotlinx.datetime 0.7.0

## Resources
- [Design Document](docs/openapi-kotlin-generator-design.md)
- [Gradle Plugin Documentation](https://docs.gradle.org/current/userguide/custom_plugins.html)
- [KotlinPoet Documentation](https://square.github.io/kotlinpoet/)
- [OpenAPI Specification](https://swagger.io/specification/)